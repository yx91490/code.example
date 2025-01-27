package io;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * http://codepub.cn/2015/04/22/How-to-load-resource-file-in-a-Jar-package-correctly/
 */
public class ClasspathResourceSample {

    public void load() {
        // 1. 从加载该类的classLoader下获取;
        //    如果该类是由bootstrapClassLoader加载，
        //    那么该方法等效于ClassLoader.getSystemResource()
        getClass().getResource("/my.conf");

        // 2. 从此类所在的包下获取资源
        getClass().getResource("my.conf");

        // 3. 等同于2, 所有的ClassLoader路径都是绝对路径，
        //    不需要参数以/作为合法的路径开始符号
        getClass().getClassLoader().getResource("my.conf");
    }

    Path getUnifiedResourcePath() {
        return getUnifiedResourcePath(getClass());
    }

    /**
     * get class's root directory, or jar file's parent directory.
     */
    Path getUnifiedResourcePath(Class<?> clazz) {
        String thisClassName = clazz.getName().replace('.', '/') + ".class";
        URL thisClassURL = clazz.getClassLoader().getResource(thisClassName);
        Objects.requireNonNull(thisClassURL);
        String classPathStr = thisClassURL.getPath();
        if ("jar".equalsIgnoreCase(thisClassURL.getProtocol())) {
            String jarPathStr = classPathStr.substring(0, classPathStr.lastIndexOf("!"));
            return Paths.get(URI.create(jarPathStr)).getParent();
        } else {
            String classRootDirStr = classPathStr.substring(0, classPathStr.length() - thisClassName.length());
            try {
                URI uri = new URL(thisClassURL.getProtocol(), null, classRootDirStr).toURI();
                return Paths.get(uri);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
