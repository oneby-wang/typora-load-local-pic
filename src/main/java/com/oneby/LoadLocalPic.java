package com.oneby;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadLocalPic {

    public static void main(String[] args) {

        // 笔记存储根目录
        String noteRootPath;

        // 从命令行读取笔记存储根目录，否则使用默认值
        if (args == null || args.length == 0) {
            noteRootPath = "<输入笔记存储路径>";
        } else {
            noteRootPath = args[0];
        }

        // MD 文件加载本地图片路径（前提：本地图片名称和阿里云 OSS 上的图片名称一致）
        doLoadLocalPic(noteRootPath);
    }

    private static void doLoadLocalPic(String destPath) {
        // 获取当前路径的File对象
        File destPathFile = new File(destPath);

        // 如果是文件，执行单个md文件的图片链接本地化，然后递归返回即可
        if (destPathFile.isFile()) {
            System.out.println(destPathFile.getName() + " 开始加载本地图片链接...");
            doLoadSingleMdLocalPic(destPath);
            System.out.println(destPathFile.getName() + " 加载本地图片完毕~~~");
            System.out.println();
            return;
        }

        // 获取当前路径下所有的子文件和路径
        File[] allFiles = destPathFile.listFiles();

        // 遍历allFiles
        for (File curFile : allFiles) {

            // 获取curFile对象是否为文件夹
            Boolean isDirectory = curFile.isDirectory();

            // 获取当前curFile对象对应的绝对路径名
            String absolutePath = curFile.getAbsolutePath();

            // 如果是文件夹
            if (isDirectory) {
                // 如果是asset文件夹，则直接调过
                if (absolutePath.endsWith(".assets")) {
                    continue;
                }
            }

            // 如果是文件夹，则继续执行递归
            doLoadLocalPic(absolutePath);
        }
    }

    private static void doLoadSingleMdLocalPic(String destMdFilePath) {
        String mdFileContent = getUseLocalPicMdContent(destMdFilePath);
        SaveMdContentToFile(destMdFilePath, mdFileContent);
    }

    private static String getUseLocalPicMdContent(String destMdFilePath) {

        // 如果不是 MD 文件，滚蛋
        Boolean isMdFile = destMdFilePath.endsWith(".md");
        if (!isMdFile) {
            return "";
        }

        // 存储md文件内容
        StringBuilder sb = new StringBuilder();

        // 当前行内容
        String curLine;

        // 装饰者模式：FileReader无法一行一行读取，所以使用BufferedReader装饰FileReader
        try (
                FileReader fr = new FileReader(destMdFilePath);
                BufferedReader br = new BufferedReader(fr);
        ) {

            // 当前行有内容
            while ((curLine = br.readLine()) != null) {

                // 图片路径存储格式：![image-20200711220145723](https://heygo.oss-cn-shanghai.aliyuncs.com/Software/Typora/Typora_PicGo_CSDN.assets/image-20200711220145723.png)
                // 正则表达式
                /*
                    ^$：匹配一行的开头和结尾
                    \[.*\]：![image-20200711220145723]
                        . ：匹配任意字符
                        * ：出现0次或多次
                    \(.+\)：(https://heygo.oss-cn-shanghai.aliyuncs.com/Software/Typora/Typora_PicGo_CSDN.assets/image-20200711220145723.png)
                        . ：匹配任意字符
                        + ：出现1次或多次
                 */
                String regex = "!\\[.*\\]\\(.+\\)";

                // 执行正则表达式
                Matcher matcher = Pattern.compile(regex).matcher(curLine);

                // 是否匹配到图片路径
                boolean isPicUrl = matcher.find();

                // 如果当前行是图片的 URL ，干他
                if (isPicUrl) {

                    // 提取图片路径前面不变的部分
                    Integer preStrEndIndex = curLine.indexOf("(");
                    String preStr = curLine.substring(0, preStrEndIndex + 1);

                    // 获取图片名称
                    Integer picNameStartIndex = curLine.lastIndexOf("/");
                    Integer curLineLength = curLine.length();
                    String picName = curLine.substring(picNameStartIndex + 1, curLineLength - 1);

                    // 获取asset文件夹的路径
                    File mdFile = new File(destMdFilePath);
                    String mdFileName = mdFile.getName();
                    Integer mdFileNameExtStartIndex = mdFileName.lastIndexOf(".");
                    String mdFileNameWithoutExt = mdFileName.substring(0, mdFileNameExtStartIndex);
                    String assetPath = mdFileNameWithoutExt + ".assets";

                    // 拼接得到本地图片的路径
                    String localPicPath = preStr + assetPath + "/" + picName + ")";
                    curLine = localPicPath;

                    System.out.println("修改图片连接：" + curLine);
                }

                sb.append(curLine + "\r\n");
            }

            // 返回 MD 文件内容
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void SaveMdContentToFile(String destMdFilePath, String mdFileContent) {

        // 不保存空文件
        if (mdFileContent == null || mdFileContent == "") {
            return;
        }

        // 执行保存
        try (FileWriter fw = new FileWriter(destMdFilePath)) {
            fw.write(mdFileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
