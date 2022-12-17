//Name: Jarrad Self
//Class: CS3642
//Assignment 3
//My image model class to gets and sets the buffered images

import java.awt.image.BufferedImage;

class ImageModel {

    private volatile BufferedImage[] images = new BufferedImage[8];

    // Getter
    public BufferedImage[] getImages() {
        return images;
    }

    // Setter
    public void setImages(int index, BufferedImage newImagePart) {
        this.images[index] = newImagePart;
    }
}