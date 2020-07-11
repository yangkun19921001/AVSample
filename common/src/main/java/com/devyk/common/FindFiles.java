package com.devyk.common;

import android.annotation.SuppressLint;
import android.util.Log;
import io.reactivex.*;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.blankj.utilcode.util.FileUtils.getFileName;

/**
 * <pre>
 *     author  : devyk on 2020-06-04 16:34
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FindFiles
 * </pre>
 */
public class FindFiles {

    private static String TAG = "FindFiles";

    private IFindFileCallback iFindFileCallback;

    /**
     * 定义文件类型
     */
    public static enum FILE_TYPE {
        DOC(".doc"),
        JPG(".jpg"),
        PCM(".pcm"),
        PNG(".png");


        private String name;

        private FILE_TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * @param dir       文件目录
     * @param list      找到的文件
     * @param file_type 文件类型
     */
    private void findFiles(File dir, List<String> list, FILE_TYPE file_type) {
        //得到某个文件夹下所有的文件
        File[] files = dir.listFiles();
        //文件为空
        if (files == null) {
            if (iFindFileCallback != null)
                iFindFileCallback.onError("no such file directory!");
            return;
        }
        //遍历当前文件下的所有文件
        for (File file : files) {
            //如果是文件夹
            if (file.isDirectory()) {
                //则递归
                findFiles(file, list, file_type);
            } else { //是文件
                if (file.getName().endsWith(file_type.getName())) {
                    list.add(file.getAbsolutePath());
                }
            }
        }


    }

    public void queryFiles(File dir, FILE_TYPE type, IFindFileCallback iFindFileCallback) {
        this.iFindFileCallback = iFindFileCallback;
        ArrayList<String> lists = new ArrayList<String>();
        //遍历所有 Dir
        findFiles(dir, lists, type);
        if (iFindFileCallback != null)
            iFindFileCallback.onFiles(lists);
    }


    public interface IFindFileCallback {
        void onFiles(List<String> lists);

        void onError(String error);
    }

    @SuppressLint("CheckResult")
    public static void getFiles(File file, List<String> list) {
        if (!file.isDirectory()) return;
        Observable.just(file)
                // 开启子线程
                .observeOn(Schedulers.io())
                .map(new Function<File, List<File>>() {
                    @Override
                    public List<File> apply(File file) throws Exception {
                        return Arrays.asList(file.listFiles());
                    }
                })
                .flatMap(new Function<List<File>, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(List<File> files) throws Exception {
                        return Observable.fromIterable(files);
                    }
                })
                // 切换回主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(files -> {
                    if (files.isDirectory()) { //如果是文件夹类型，递归
                        getFiles(files, list);
                    } else if (files.getName().contains(".pcm")) { // 如果是需要的格式，向下传到结果
                        list.add(files.getAbsolutePath());
                    }
                });
    }


    public static List<String> test() {
        List<String> lists = new ArrayList<>();
        getFiles(new File("sdcard"), lists);
        return lists;
    }


    /**
     * 找到手里里面所有的【？】文件
     */
    public static void main() {
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                FindFiles files = new FindFiles();
                files.queryFiles(new File("/sdcard/"), FindFiles.FILE_TYPE.PCM, new IFindFileCallback() {
                    @Override
                    public void onFiles(List<String> lists) {
                        emitter.onNext(lists);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "error" + error);
                        emitter.onError(new RuntimeException(error));
                        emitter.onComplete();
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "订阅成功");
                    }

                    @Override
                    public void onNext(List<String> list) {
                        Log.e(TAG, list.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "执行完成!");
                    }
                });


    }

}
