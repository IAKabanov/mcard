package ru.kai.mcard;
// объектная модель для RecyclerViewAdapter

/**
 * Created by akabanov on 20.05.2016.
 */
public class ItemPhotoObject {
    private int photoID;  // идентификатор из таблицы (для более быстрого поиска с целью удаления или изменения)
    private int photoType;  // из камеры (0) фотография или из галереи (1) (type - int)
    private String photoName;
    private String photoPath;

    public ItemPhotoObject(int photoID, int photoType, String photoName, String photoPath) {
        this.photoID = photoID;
        this.photoType = photoType;
        this.photoName = photoName;
        this.photoPath = photoPath;
    }

    public int getPhotoID() {
        return photoID;
    }

    public int getPhotoType() {
        return photoType;
    }

    public String getPhotoName() {
        return photoName;
    }

    public String getPhotoPath() {
        return photoPath;
    }
}
