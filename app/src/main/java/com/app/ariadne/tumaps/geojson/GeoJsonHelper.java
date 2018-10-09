package com.app.ariadne.tumaps.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.geojson.GeoJsonFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//import static com.roomr.ariadne.tumnavigator.ParticleDeadReckoning.INTERPOLATION_STEP;

public class GeoJsonHelper {
    private static final double INTERPOLATION_STEP = 0.1;
    public static String findElementType(GeoJsonFeature feature) {
        String type = "";
        Iterable props = feature.getProperties();
        for (Object prop : props) {
            String property = prop.toString();
            if (property.contains("highway")) {
                return(getValueOfProperty(property));
            }
        }
        return type;
    }

    public static String getValueOfProperty(String property) {
        StringTokenizer multiTokenizer = new StringTokenizer(property, "=");
        int index = 0;
        String value = "";
        while (multiTokenizer.hasMoreTokens()) {
            if (index == 1) {
                value = multiTokenizer.nextToken();
            } else {
                multiTokenizer.nextToken();
            }
            index++;
        }
        return value;
    }

    public static ArrayList<Integer> getLevelOfElement(String property) {
        StringTokenizer multiTokenizer = new StringTokenizer(property, "=");
        ArrayList<Integer> levels = new ArrayList<>();
        int index = 0;
        int level;
        while (multiTokenizer.hasMoreTokens()) {
            if (index == 1) {
                String token = multiTokenizer.nextToken();
//                System.out.println("Token = " + token + "!!!!!!!!!!!!!!!!!!!!!!!");
                StringTokenizer nestedTokenizer = new StringTokenizer(token, ";");
                if (!nestedTokenizer.hasMoreTokens()) {
                    level = Integer.valueOf(token);
                    levels.add(level);
                }
                while (nestedTokenizer.hasMoreTokens()) {
                    level = Integer.valueOf(nestedTokenizer.nextToken());
                    levels.add(level);
                }
            } else {
                multiTokenizer.nextToken();
            }
            index++;
        }
        return levels;
    }

    public static ArrayList<ArrayList<LatLngWithTags>> findStraightLines(ArrayList<ArrayList<LatLngWithTags>> paths) {
        ArrayList<ArrayList<LatLngWithTags>> straightLines = new ArrayList<>();
        double headingThreshold = 5.0;
        for (ArrayList<LatLngWithTags> path: paths) {
            ArrayList<LatLngWithTags> straightLine = new ArrayList<>();
            LatLngWithTags start = path.get(0);
            LatLngWithTags end = path.get(1);
            straightLine.add(start);
            straightLine.add(end);
            double currentHeading = SphericalUtil.computeHeading(start.getLatlng(), end.getLatlng());
            if (path.size() > 2) {
                for (int i = 2; i < path.size(); i++) {
                    start = end;
                    end = path.get(i);
                    if (Math.abs(currentHeading - SphericalUtil.computeHeading(start.getLatlng(), end.getLatlng())) < headingThreshold) {
                        straightLine.add(end);
                    } else {
                        straightLines.add(straightLine);
                        straightLine = new ArrayList<>();
                        straightLine.add(start);
                        straightLine.add(end);
                        currentHeading = SphericalUtil.computeHeading(start.getLatlng(), end.getLatlng());
                    }
                }
            }
            straightLines.add(straightLine);
        }
        return straightLines;
    }

    public static ArrayList<ArrayList<LatLngWithTags>> interpolateLines(ArrayList<ArrayList<LatLngWithTags>> lines) {
        ArrayList<ArrayList<LatLngWithTags>> interpolatedLines = new ArrayList<>();
        for (ArrayList<LatLngWithTags> line: lines) {
            ArrayList<LatLngWithTags> totalInterpolatedLine = new ArrayList<>();
            for (int i = 0; i < line.size() - 1; i++) {
                LatLngWithTags start = line.get(i);
                LatLngWithTags end = line.get(i+1);
                ArrayList<LatLngWithTags> interpolatedLine = interpolateLine(start, end);
                totalInterpolatedLine.addAll(interpolatedLine);
            }
            interpolatedLines.add(totalInterpolatedLine);
        }
        return interpolatedLines;
    }



    private static ArrayList<LatLngWithTags> interpolateLine(LatLngWithTags start, LatLngWithTags end) {
        double step = INTERPOLATION_STEP;
        double distance = SphericalUtil.computeDistanceBetween(start.getLatlng(), end.getLatlng());
        double fraction = step / distance;
        double numOfSteps = distance / step;
        ArrayList<LatLngWithTags> interpolatedLine = new ArrayList<>();
        interpolatedLine.add(start);
        LatLng tempStart = start.getLatlng();
        String level = start.getLevel();

        for (int i = 0; i < Math.floor(numOfSteps) - 1; i++) {
//            LatLng intermediateStep = SphericalUtil.interpolate(start, end, (i+1) * fraction);
            LatLng intermediateStep = SphericalUtil.computeOffset(tempStart, step, SphericalUtil.computeHeading(start.getLatlng(), end.getLatlng()));
//            System.out.println("Start: " + start.latitude + ", " + start.longitude + ", step: " + intermediateStep.latitude + ", " + intermediateStep.longitude + ", End: " + end.latitude + ", " + end.longitude);
//            System.out.println("End: " + end.latitude + ", " + end.longitude + ", fraction: " + (i+1) * fraction);
            interpolatedLine.add(new LatLngWithTags(intermediateStep, level, intermediateStep.toString()));
            tempStart = intermediateStep;
        }
        interpolatedLine.add(end);
        return interpolatedLine;
    }

    public static ArrayList<ArrayList<LatLng>> interpolateLinesLatLng(ArrayList<ArrayList<LatLng>> lines) {
        ArrayList<ArrayList<LatLng>> interpolatedLines = new ArrayList<>();
        for (ArrayList<LatLng> line: lines) {
            ArrayList<LatLng> totalInterpolatedLine = new ArrayList<>();
            for (int i = 0; i < line.size() - 1; i++) {
                LatLng start = line.get(i);
                LatLng end = line.get(i+1);
                ArrayList<LatLng> interpolatedLine = interpolateLineLatLng(start, end);
                totalInterpolatedLine.addAll(interpolatedLine);
            }
            interpolatedLines.add(totalInterpolatedLine);
        }
        return interpolatedLines;
    }

    public static ArrayList<LatLng> interpolateLineLatLng(LatLng start, LatLng end) {
        double step = 0.1;
        double distance = SphericalUtil.computeDistanceBetween(start, end);
        double fraction = step / distance;
        double numOfSteps = distance / step;
        ArrayList<LatLng> interpolatedLine = new ArrayList<>();
        interpolatedLine.add(start);
        LatLng tempStart = start;
        for (int i = 0; i < Math.floor(numOfSteps); i++) {
//            LatLng intermediateStep = SphericalUtil.interpolate(start, end, (i+1) * fraction);
            LatLng intermediateStep = SphericalUtil.computeOffset(tempStart, step, SphericalUtil.computeHeading(start, end));
//            System.out.println("Start: " + start.latitude + ", " + start.longitude + ", step: " + intermediateStep.latitude + ", " + intermediateStep.longitude + ", End: " + end.latitude + ", " + end.longitude);
//            System.out.println("End: " + end.latitude + ", " + end.longitude + ", fraction: " + (i+1) * fraction);
            interpolatedLine.add(intermediateStep);
            tempStart = intermediateStep;
        }
        interpolatedLine.add(end);
        return interpolatedLine;
    }


    public static ArrayList<LatLng> ListToArrayList(List<LatLng> routePoints) {
        ArrayList<LatLng> route = new ArrayList<>();
        for (LatLng point : routePoints) {
            route.add(point);
        }
        return route;
    }

}
