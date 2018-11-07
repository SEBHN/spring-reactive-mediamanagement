package de.hhn.mvs;

import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class can be deleted after we have real data
 */
public class MediaCreator {

    private List<Media> dummyMedia;
    private static MediaCreator instance;

    public static MediaCreator getInstance() {
        if (MediaCreator.instance == null) {
            MediaCreator.instance = new MediaCreator();
        }
        return MediaCreator.instance;
    }

    private MediaCreator() {
        dummyMedia = new ArrayList<>();
        fillDummyMedia();
    }

    public List<Media> getDummyMedia() {
        return dummyMedia;
    }

    private void fillDummyMedia() {
        for (int i = 0; i < 10; i++) {
            dummyMedia.add(createDummyMedia(i));
        }
    }

    private Media createDummyMedia(int number) {
        if (number % 2 == 0) {
            return new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat " + number, "cat.jpg", ".jpg", "", new Tag("1", "cats"), new Tag("3", "cute"));
        } else {
            return new MediaImpl(UUID.randomUUID().toString(), "My fabulous dog" + +number, "cat.jpg", ".jpg", "", new Tag("2", "dogs"));
        }

    }


}
