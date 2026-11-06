//package org.firstinspires.ftc.teamcode.subsystems;
//
//import android.util.Size;
//
//import com.arcrobotics.ftclib.command.SubsystemBase;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//import com.qualcomm.robotcore.util.SortOrder;
//
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.vision.VisionPortal;
//import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
//import org.firstinspires.ftc.vision.opencv.ColorRange;
//import org.firstinspires.ftc.vision.opencv.ImageRegion;
//import org.opencv.core.RotatedRect;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BlobVision extends SubsystemBase {
//    public static double cameraHeight = 7.5;
//    // tune this
//    public static double pixelsToInches = 0.1174;
//    public static double cameraAngle = 15;
//
//    ColorBlobLocatorProcessor colorLocator;
//
//    VisionPortal portal;
//
//    public BlobVision(final HardwareMap hMap, ColorRange color) {
//        colorLocator = new ColorBlobLocatorProcessor.Builder()
//                .setTargetColorRange(color)         // use a predefined color match
//                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)    // exclude blobs inside blobs
//                .setRoi(ImageRegion.entireFrame())  // search central all of the camera view
//                .setDrawContours(true)                        // Show contours on the Stream Preview
//                .setBlurSize(5)                               // Smooth the transitions between different colors in image
//                .build();
//
//        portal = new VisionPortal.Builder()
//                .addProcessor(colorLocator)
//                .setCameraResolution(new Size(320, 240))
//                .setCamera(hMap.get(WebcamName.class, "Webcam 1"))
//                .build();
//    }
//
//    // Returns the x-coordinate (relative to the camera) of the largest blob
//    public double getLargestBlobXCoord() {
//        // Read the current list
//        List<ColorBlobLocatorProcessor.Blob> blobs = colorLocator.getBlobs();
//
//        /*
//         * The list of Blobs can be filtered to remove unwanted Blobs.
//         *   Note:  All contours will be still displayed on the Stream Preview, but only those that satisfy the filter
//         *          conditions will remain in the current list of "blobs".  Multiple filters may be used.
//         *
//         * Use any of the following filters.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByArea(minArea, maxArea, blobs);
//         *   A Blob's area is the number of pixels contained within the Contour.  Filter out any that are too big or small.
//         *   Start with a large range and then refine the range based on the likely size of the desired object in the viewfinder.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByDensity(minDensity, maxDensity, blobs);
//         *   A blob's density is an indication of how "full" the contour is.
//         *   If you put a rubber band around the contour you would get the "Convex Hull" of the contour.
//         *   The density is the ratio of Contour-area to Convex Hull-area.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByAspectRatio(minAspect, maxAspect, blobs);
//         *   A blob's Aspect ratio is the ratio of boxFit long side to short side.
//         *   A perfect Square has an aspect ratio of 1.  All others are > 1
//         */
//        ColorBlobLocatorProcessor.Util.filterByArea(50, 20000, blobs);  // filter out very small blobs.
//
//        /*
//         * The list of Blobs can be sorted using the same Blob attributes as listed above.
//         * No more than one sort call should be made.  Sorting can use ascending or descending order.
//         *     ColorBlobLocatorProcessor.Util.sortByArea(SortOrder.DESCENDING, blobs);      // Default
//         *     ColorBlobLocatorProcessor.Util.sortByDensity(SortOrder.DESCENDING, blobs);
//         *     ColorBlobLocatorProcessor.Util.sortByAspectRatio(SortOrder.DESCENDING, blobs);
//         */
//
//        ColorBlobLocatorProcessor.Util.sortByArea(SortOrder.DESCENDING, blobs);
//
//        // Ignore blobs that have a y-coordinate less than 120; this means that the blobs are too far away
//        blobs = filterByYCoord(120, blobs);
//        // Obtain the largest blob
//        RotatedRect boxFit = blobs.get(0).getBoxFit();
//        // Calculate the x-coordinate (relative to the camera) of the largest blob
//        return calculateXCoord(cameraHeight, cameraAngle, (int) boxFit.center.y, pixelsToInches);
//    }
//
//    double calculateXCoord(double height, double angle, double pixelY, double pixelsToInches) {
//        double xOffset = height / Math.tan(Math.toRadians(angle));
//        // center of the camera relative to the (x, y) of the camera on the robot is (xOffset, 0)
//        return xOffset + (pixelY - 120) * pixelsToInches;
//    }
//
//    // returns the Y-coordinate (relative to the camera) of the largest blob
//    public double getLargestBlobYCoord() {
//        // Read the current list
//        List<ColorBlobLocatorProcessor.Blob> blobs = colorLocator.getBlobs();
//
//        /*
//         * The list of Blobs can be filtered to remove unwanted Blobs.
//         *   Note:  All contours will be still displayed on the Stream Preview, but only those that satisfy the filter
//         *          conditions will remain in the current list of "blobs".  Multiple filters may be used.
//         *
//         * Use any of the following filters.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByArea(minArea, maxArea, blobs);
//         *   A Blob's area is the number of pixels contained within the Contour.  Filter out any that are too big or small.
//         *   Start with a large range and then refine the range based on the likely size of the desired object in the viewfinder.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByDensity(minDensity, maxDensity, blobs);
//         *   A blob's density is an indication of how "full" the contour is.
//         *   If you put a rubber band around the contour you would get the "Convex Hull" of the contour.
//         *   The density is the ratio of Contour-area to Convex Hull-area.
//         *
//         * ColorBlobLocatorProcessor.Util.filterByAspectRatio(minAspect, maxAspect, blobs);
//         *   A blob's Aspect ratio is the ratio of boxFit long side to short side.
//         *   A perfect Square has an aspect ratio of 1.  All others are > 1
//         */
//        ColorBlobLocatorProcessor.Util.filterByArea(50, 20000, blobs);  // filter out very small blobs.
//
//        /*
//         * The list of Blobs can be sorted using the same Blob attributes as listed above.
//         * No more than one sort call should be made.  Sorting can use ascending or descending order.
//         *     ColorBlobLocatorProcessor.Util.sortByArea(SortOrder.DESCENDING, blobs);      // Default
//         *     ColorBlobLocatorProcessor.Util.sortByDensity(SortOrder.DESCENDING, blobs);
//         *     ColorBlobLocatorProcessor.Util.sortByAspectRatio(SortOrder.DESCENDING, blobs);
//         */
//
//        ColorBlobLocatorProcessor.Util.sortByArea(SortOrder.DESCENDING, blobs);
//
//        blobs = filterByYCoord(120, blobs);
//
//        // Obtain the largest blob
//        RotatedRect boxFit = blobs.get(0).getBoxFit();
//        return calculateYCoord((int) boxFit.center.x, pixelsToInches);
//    }
//
//    double calculateYCoord(double pixelX, double pixelsToInches) {
//        return (pixelX - 320 / 2) * pixelsToInches;
//    }
//
//    // Filter out any blobs that are too far away to grab.
//    // If the y-coordinate of a blob is too small, that means it's farther away from the submersible.
//    public List<ColorBlobLocatorProcessor.Blob> filterByYCoord(double yCoord, List<ColorBlobLocatorProcessor.Blob> blobs)
//    {
//        ArrayList<ColorBlobLocatorProcessor.Blob> toRemove = new ArrayList<>();
//
//        for(ColorBlobLocatorProcessor.Blob b : blobs)
//        {
//            if (b.getBoxFit().center.y < yCoord)
//            {
//                toRemove.add(b);
//            }
//        }
//
//        blobs.removeAll(toRemove);
//        return blobs;
//    }
//}
