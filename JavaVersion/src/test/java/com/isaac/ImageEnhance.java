package com.isaac;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.isaac.models.ALTMRetinex;
import com.isaac.models.DarkChannelPriorDehaze;
import com.isaac.models.FusionEnhance;
import com.isaac.models.OptimizedContrastEnhance;
import com.isaac.models.RemoveBackScatter;
import com.isaac.utils.ImShow;

import net.sf.ij_plugins.dcraw.DCRawException;
import net.sf.ij_plugins.dcraw.DCRawReader;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageEnhance {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
	// ALTM Retinex
    private static final int r1 = 10;
    private static final double eps1 = 0.01;
    private static final double eta1 = 36.0;
    private static final double lambda1 = 10.0;
    private static final double krnlRatio1 = 0.01;
    
    public static void main (String[] args) {
    	String imgPath = "E:\\WORK\\Joe Jesuele\\Resources\\Datasets\\Raw Images\\Aerial only 2\\Single exp_png";
    	File imgFile = new File(imgPath);
    	if(imgFile.exists())
    	{
            System.out.println("Starting ...");

            if(imgFile.isDirectory())
    		{
    	    	String outPath = imgPath + "_edited";
    	    	File outFile = new File(outPath);
    	        if(!outFile.exists())
    	        	outFile.mkdirs();
    	        
    	        int NumberOfImagesInAPage = 5;
    	        int PageIndex = 3;

    	        int index = 0;
                for(File file : imgFile.listFiles())
                {
                	if(index >= NumberOfImagesInAPage * PageIndex && index < NumberOfImagesInAPage * PageIndex + NumberOfImagesInAPage)
                	{
                		String outFilename = outPath + "\\"
    							+ file.getName().substring(0, file.getName().length() - 4)
    							+ "_edited" + ".png";
                    	EnhanceImage(file.getPath(), outFilename);
                	}
                	index++;
                }
    		}
    		else
    		{
        		String outFilename = imgPath.substring(0, imgPath.length() - 4)
						+ "_edited" + ".png";
            	EnhanceImage(imgPath, outFilename);
    		}

    		System.out.println("Completed ...");
    	}
    	else
    	{
    		System.out.println("File or Directory \"" + imgPath + "\" does not exist");
    	}    	
    }
    
    public static void EnhanceImage(String src, String dest)
    {
    	// Read File
        Mat image = Imgcodecs.imread(src, Imgcodecs.CV_LOAD_IMAGE_COLOR);

        // Change Saturation
//        ChangeSaturation(image, saturationValue);
		
        // ALTM Retinex
		image = ALTMRetinex.enhance(image, r1, eps1, eta1, lambda1, krnlRatio1);
		
		// Change Brightness
//		ChangeBrightness(image, 20);
		
		// Save File
		Imgcodecs.imwrite(dest, image);
        System.out.println("Saved Image: " + dest);
    }
    
    public static void ChangeSaturation(Mat image, float value)
    {
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);
    	for(int i = 0; i < image.height(); i++)
    	{
    		for(int j = 0; j < image.width(); j++)
    		{
    			double[] values = image.get(i, j);
    			values[1] += value;
    			image.put(i, j, values);
    		}
    	}
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_HSV2BGR);
    }
    
    public static void ChangeBrightness(Mat image, float value)
    {
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HLS);
    	for(int i = 0; i < image.height(); i++)
    	{
    		for(int j = 0; j < image.width(); j++)
    		{
    			double[] values = image.get(i, j);
    			values[1] += value;
    			image.put(i, j, values);
    		}
    	}
    	Imgproc.cvtColor(image, image, Imgproc.COLOR_HLS2BGR);
    }
    
//    public static void OverlayImage(Mat image, float opacity)
//    {
//    	for(int i = 0; i < image.height(); i++)
//    	{
//    		for(int j = 0; j < image.width(); j++)
//    		{
//    			double[] values = image.get(i, j);
//    			values[1] += value;
//    			image.put(i, j, values);
//    		}
//    	}
//    }
    
//    public static void GetImage(String imgPath)
//    {
//    	// Input file
//    	final File inFile = new File(imgPath);
//    	
//    	// dcraw wrapper
//    	final DCRawReader dcRawReader = new DCRawReader();
//    	 
//    	// Listen to output messages
//    	dcRawReader.addLogListener(new DCRawReader.LogListener() {
//    	    @Override
//    	    public void log(String message) {
//    	        System.out.println("message = " + message);
//    	    }
//    	});
//    	 
//    	// Execute dcraw
//    	try {
//			dcRawReader.executeCommand(new String[]{
//			        "-v", // Print verbose messages
//			        "-w", // Use camera white balance, if possible
//			        "-T", // Write TIFF instead of PPM
//			        "-j", // Don't stretch or rotate raw pixels
//			        "-W", // Don't automatically brighten the image
//			        inFile.getAbsolutePath()});
//		} catch (DCRawException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	 
//    	// Cleanup
//    	dcRawReader.removeAllLogListeners();    	 
//    }
    
//    public static void GetWidthHeight(String imgPath)
//    {
//        File file = new File(imgPath);
//        System.out.println(imgPath);
//        try {
//            Metadata metadata = ImageMetadataReader.readMetadata(file);
//            for (Directory directory : metadata.getDirectories()) {
//                for (Tag tag : directory.getTags()) {
//                	if(tag.getTagName().compareTo("Image Width") == 0)
//                	{
//                		String[] words = tag.getDescription().split(" ");
//                		Width = Integer.parseInt(words[0]);
//                		System.out.println("Width: " + Width);
//                	}
//                	if(tag.getTagName().compareTo("Image Height") == 0)
//                	{
//                		String[] words = tag.getDescription().split(" ");
//                		Height = Integer.parseInt(words[0]);
//                		System.out.println("Height: " + Height);
//                	}
//                }
//            }
//        } catch (ImageProcessingException e) {
//            System.out.print(e);
//        } catch (IOException e) {
//            System.out.print(e);
//        }
//    }
}
