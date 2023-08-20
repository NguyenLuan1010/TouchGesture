package com.dyson.tech.touchgesture.view;

import com.dyson.tech.touchgesture.model.Apps;

import java.io.File;
import java.util.List;

public abstract class AuthenticationCallBack {
    public interface AddGestureCallBack {
        void addSuccess(String message);

        void addFail(String message);
    }

    public interface GetGestureFilesCallBack {
        void allFile(List<Apps> appsList);

        void nothingFile(String message);
    }

    public interface EditGestureFileCallBack {
        void editSuccess(Apps updatedApp,String message);

        void editFail(String message);
    }

    public interface CheckGestureEqualCallBack{
        void isEqual();
    }
}
