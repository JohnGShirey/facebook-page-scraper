package cmdline;

import common.Config;
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
            System.out.println(username);
            AlbumsCollector albumsCollector = new AlbumsCollector(username);
            albumsCollector.collectAlbums();
            System.out.println(albumsCollector.getAlbums().size());
            for(Album album: albumsCollector.getAlbums())
            {
                album.writeJson();
                PhotosCollector photosCollector = new PhotosCollector(username, album.getId());
                photosCollector.collectPhotos();
                for(Photo photo: photosCollector.getPhotos())
                {
                    photo.writeJson();
                    photo.writeImages();
                }
            }
        }
    }
}
