// IFileExplorerService.aidl
package com.kuss.krude.shizuku;

// Declare any non-default types here with import statements
import com.kuss.krude.shizuku.bean.BeanFile;

interface IFileExplorerService {
    List<BeanFile> listFiles(String path);
}