package com.omar.assignment;

import com.omar.common.Util;

public class Demo {
    public static void main(String[] args) {
        FileService fs = new FileServiceImpl();
        String path = "src/main/java/com/omar/assignment/omar-file";

        fs.write(path, "omar is creating a file...")
                .subscribe(Util.subscriber());

        fs.read(path)
                .subscribe(Util.subscriber());

        fs.delete(path)
                .subscribe(Util.subscriber());
    }
}
