package cmdline;

import common.Config;
import common.Util;
import common.album.Album;
import common.album.AlbumsCollector;
import common.album.Photo;
import common.album.PhotosCollector;

public class ImageCollector
{
    public static void main(String[] args)
    {
        Config.initImageCollector();

        for(String username: Config.pages)
        {
            System.out.println(Util.getDbDateTimeEst() + " started downloading albums from page: " + username);

            AlbumsCollector albumsCollector = new AlbumsCollector(username);
            albumsCollector.collectAlbums();

            System.out.println(Util.getDbDateTimeEst() + " found " + albumsCollector.getAlbums().size() + " albums");

            for(Album album: albumsCollector.getAlbums())
            {
                album.writeJson();

                System.out.println(Util.getDbDateTimeEst() + " downloading photos from album: " + album.getName());

                PhotosCollector photosCollector = new PhotosCollector(username, album.getId());
                photosCollector.collectPhotos();
                for(Photo photo: photosCollector.getPhotos())
                {
                    photo.writeJson();
                    photo.writeImages();
                }

                System.out.println(Util.getDbDateTimeEst() + " downloaded " + photosCollector.getPhotos().size() + " photos");
            }

            System.out.println(Util.getDbDateTimeEst() + " completed downloading albums from page: " + username);
        }
    }
}
