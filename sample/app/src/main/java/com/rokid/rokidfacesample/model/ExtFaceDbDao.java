package com.rokid.rokidfacesample.model;

import com.rokid.facelib.db.FaceDbDao;

public class ExtFaceDbDao extends FaceDbDao{

    public byte[] bytes;

    public ExtFaceDbDao() {

    }

    public ExtFaceDbDao(String n) {
        super(n);
    }

}
