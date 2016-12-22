package org.hisp.dhis.ivb.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

public class ImageAction
    extends ActionSupport
    implements ServletRequestAware
{

	
    private static final long serialVersionUID = 1288992988151668635L;

    byte[] imageInByte = null;

    private String imagePath;

    public void setImagePath( String imagePath )
    {
        this.imagePath = imagePath;
    }

    String imageId;

    public String getImageId()
    {
        return imageId;
    }

    public void setImageId( String imageId )
    {
        this.imageId = imageId;
    }

    public ImageAction()
    {
    }

    public String execute()
    {
        return SUCCESS;
    }

    public byte[] getCustomImageInBytes()
    {
        BufferedImage originalImage;
        
        try
        {
            File imageFile = new File( imagePath );
                        
            if( !imageFile.exists() )
            {
                imagePath = System.getenv( "DHIS2_HOME" ) + File.separator + "flags" + File.separator + "NO_IMG.png";
                imageFile = new File( imagePath );
            }
            
            originalImage = ImageIO.read( new File( imagePath ) );
            
            // convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( originalImage, "png", baos );
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
        }
        catch ( IOException e )
        {            
            e.printStackTrace();
        }

        return imageInByte;
    }

    public String getCustomContentType()
    {
        return "image/png";
    }

    public String getCustomContentDisposition()
    {
        return "anyname.png";
    }

    @Override
    public void setServletRequest( HttpServletRequest request )
    {

    }
}