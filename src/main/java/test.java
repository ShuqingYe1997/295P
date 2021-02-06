import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: test
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-6
 */
public class test {
    public static void main(String[] args) {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();

        for (int i = 1; i <= 10; i++)
            list1.add(i+"");
        for (int i = 9; i >= 0; i--)
            list2.add(i+"");

        list1.addAll(list2);
        System.out.println(list1);
    }
}
