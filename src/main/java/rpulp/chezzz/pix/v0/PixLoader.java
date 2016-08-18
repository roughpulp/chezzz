package rpulp.chezzz.pix.v0;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.regex.Pattern;

public class PixLoader {

    private static final String RESOURCES_PATH = PixLoader.class.getPackage().getName().replaceAll(Pattern.quote("."), "/");

    public static BufferedImage load_1_pix(final String name) throws Exception {
        final String path = RESOURCES_PATH + "/" + name;
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new IllegalStateException("Could not find resource [" + path + "]");
        }
        return ImageIO.read(url);
    }

}
