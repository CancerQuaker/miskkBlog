import cn.hutool.crypto.digest.BCrypt;

import java.util.Scanner;

/**
 * 生成 BCrypt 密码哈希（用于更新 sql/01_miskk_user.sql 中的管理员密码）。
 *
 * 运行方式（在 backend 目录 mvn compile 之后）：
 *   jshell --class-path ~/.m2/repository/cn/hutool/hutool-all/5.8.47/hutool-all-5.8.47.jar tools/GenBcryptHash.jsh
 * 或直接用 IDEA 运行本类（需将 hutool 加入 classpath）。
 */
public class GenBcryptHash {

    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "admin123";
        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希: " + BCrypt.hashpw(password));
        System.out.println("校验结果: " + BCrypt.checkpw(password, BCrypt.hashpw(password)));
    }
}
