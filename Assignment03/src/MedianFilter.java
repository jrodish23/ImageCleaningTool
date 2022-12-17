//Name: Jarrad Self
//Class: CS3642
//Assignment 3
//My main class that splits the image, looks at pixels and sorts pixels (median filters), and puts image back together. 

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;

class MedianFilter {

    private static final int  ROWS = 1; //since we are using two threads we use 1 row and 2 columns, we could add more but no need
    private static final int COLUMNS = 2;

    private static Parallelization parallelization = new Parallelization();  //parallelization class

    static public ImageModel imageModel = new ImageModel(); //image model
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        long startTime = System.nanoTime(); //timing the time it takes to process the photo for evaluation

        BufferedImage img = ImageIO.read(MedianFilter.class.getResource("resources/images/GoldHill_15per.jpg")); //importing noisy image to get corrected.
       
        imageSplitter(img); //splitting the image into the rows and columns above

        parallelization.concurrentMedianFilter(); //executing the parallel median filter

        joinImage(); //getting the filtered image and putting back together
       
        System.out.println("Total time taken to clean noisy image: " + (System.nanoTime() - startTime) / 1e9); //outputing the amount of time it took to clean the noisy image

    }

    public static void imageSplitter(BufferedImage image) { //imageSplitter class that splits the image in the amount of parts needed 

        BufferedImage[] splitImageParts = new BufferedImage[ROWS * COLUMNS]; //processing the image before split
        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        
        Graphics g = bi.createGraphics();
        g.drawImage(image, 0, 0, null);
        
        int width = bi.getWidth();
        int height = bi.getHeight();
        int pos = 0;
        int swidth = width / COLUMNS;
        int sheight = height / ROWS;

        for (int j = 0; j < COLUMNS; j++) { //splitting the image into the input rows and columns input above
            for (int i = 0; i < ROWS; i++) {
                BufferedImage bimg = bi.getSubimage(j * swidth, i * sheight, swidth, sheight); //saving the image into an array
                splitImageParts[pos] = bimg;
                pos++;
            }
        }

        for (int i = 0; i < splitImageParts.length; i++) { //putting the image parts in the setter array
            imageModel.setImages(i, splitImageParts[i]);
        }
    }

    public static void joinImage() throws IOException { //joins the filtered image parts and saves as new image

        BufferedImage[] imageItems = imageModel.getImages(); //gets the images
 
        int chunkWidth, chunkHeight; //defines the size of the images
        int type;
        type = imageItems[0].getType();
        chunkWidth = imageItems[0].getWidth();
        chunkHeight = imageItems[0].getHeight();

        BufferedImage output = new BufferedImage(chunkWidth*COLUMNS, chunkHeight*ROWS, type); //initializing filtered image

        int num = 0;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                output.createGraphics().drawImage(imageItems[num], chunkWidth * j, chunkHeight * i, null);
                num++;
            }
        }

        ImageIO.write(output, "jpg", new File("cleanimage1.jpg")); //creating the image file as a .jpg
    }
}

class Parallelization {//filtering images parallel

    public void concurrentMedianFilter() { //using threads to run sequential median filter

        BufferedImage[] imageItem = MedianFilter.imageModel.getImages();

        Thread thread1 = new Thread(() -> {
            BufferedImage filteredImage = sequentialMedianFilter(imageItem[0]);
            MedianFilter.imageModel.setImages(0, filteredImage);
        });

        Thread thread2 = new Thread(() -> {
            BufferedImage filteredImage = sequentialMedianFilter(imageItem[1]);
            MedianFilter.imageModel.setImages(1, filteredImage);
        });
              
        thread1.start(); //starts the downloads
        thread2.start();

        
        try { //waiting for both to finish
            thread1.join();
            thread2.join();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage sequentialMedianFilter(BufferedImage img) {  //sequential median filter used by the parallel implementation

        int[] R = new int[9]; //setting up arrays for the pixels surrounding the pixel in 3 colors.
        int[] B = new int[9];
        int[] G = new int[9];
        Color[] pixel = new Color[9];

        for (int i = 1; i < img.getWidth() - 1; i++) //looping through the width and height of image, gathering the values to pick the median of the pixels
            for (int j = 1; j < img.getHeight() - 1; j++) {
                pixel[0] = new Color(img.getRGB(i - 1, j - 1));
                pixel[1] = new Color(img.getRGB(i - 1, j));
                pixel[2] = new Color(img.getRGB(i - 1, j + 1));
                pixel[3] = new Color(img.getRGB(i, j + 1));
                pixel[4] = new Color(img.getRGB(i + 1, j + 1));
                pixel[5] = new Color(img.getRGB(i + 1, j));
                pixel[6] = new Color(img.getRGB(i + 1, j - 1));
                pixel[7] = new Color(img.getRGB(i, j - 1));
                pixel[8] = new Color(img.getRGB(i, j));
                for (int k = 0; k < 9; k++) {
                    R[k] = pixel[k].getRed();
                    B[k] = pixel[k].getBlue();
                    G[k] = pixel[k].getGreen();
                }
                Arrays.sort(R);
                Arrays.sort(G);
                Arrays.sort(B);
                img.setRGB(i, j, new Color(R[4], B[4], G[4]).getRGB());
            }

        return img; //returning the image
    }

}
