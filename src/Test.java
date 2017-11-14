import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 类描述：
 *
 * @author Ethan
 * @date 2017年11月14日
 * 
 * 修改描述：
 * @modifier
 */
public class Test {
    
    static String in = "D:\\workspace_mars\\test\\src\\in.csv";
    static String out = "D:\\workspace_mars\\test\\src\\out.sql";
    
    static String org_code = "11360101";
    
    /**
     * 
     * 方法描述: 根据原始坐标数据生成国家局标准坐标数据、百度坐标数据SQL
     *          需要提供in.csv原始数据文件
     *          生成可执行SQL文件为out.sql
     * 
     * @param args void
     * @author Ethan 2017年11月14日
     *
     * 修改描述：
     * @modifier
     */
    public static void main(String[] args) {
        
        System.out.println("Work begin!");
        
        List<String> result_list = new ArrayList<String>();
        
        try {
            List dataList = file2List(0);
            int len = dataList.size();
            double org_lon, org_lan, lon, lan, net_lon, net_lan;
            double[] td;
            List line;
            String id, sql;
            StringBuilder sBuilder;
            for (int i = 1; i < len; i++) {
                line = (List) dataList.get(i);
                id = (String)line.get(0);
                org_lon = Double.parseDouble((String)line.get(1));
                org_lan = Double.parseDouble((String)line.get(2));
                td = CoordinateUtil.wgsToGcj(org_lon, org_lan);
                lon = td[0];
                lan = td[1];
                td = CoordinateUtil.wgsToBd(org_lon, org_lan);
                net_lon = td[0];
                net_lan = td[1];
                
                sBuilder = new StringBuilder();
                sBuilder.append("UPDATE CA_SHOP_INFO_EXT SET LONGITUDE = ").append(lon);
                sBuilder.append(" AND LATITUDE = ").append(lan);
                sBuilder.append(" AND NET_LONGITUDE = ").append(net_lon);
                sBuilder.append(" AND NET_LATITUDE = ").append(net_lan);
                sBuilder.append(" WHERE ORG_CODE = '").append(org_code).append("'");
                sBuilder.append(" AND SHOP_ID = '").append(id).append("'").append(";");
                
                sql = sBuilder.toString();
                
                System.out.println("第 " + i + "行， SQL: " + sql);
                
                result_list.add(sql);
                
            }
            
            out(result_list);
            
            System.out.println("Work done!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /************************************如下可不看*****************************************/
    
    /**
     * 
     * 方法描述: output to file
     * 
     * @param l void
     * @author Ethan 2017年11月14日
     */
    private static void out(String s) {
        try {
            File output = new File(out);
            BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), false));
            out.write(s);
            out.close();
        } catch (IOException e) {
            System.out.println("error" + e);
        }
    }
    
    /**
     * 
     * 方法描述: output to file
     * 
     * @param l void
     * @author Ethan 2017年11月14日
     */
    private static void out(List l) {
        try {
            File output = new File(out);
            BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), false));
            int len = l.size();
            for (int i = 0; i < len; i++) {
                out.write((String) l.get(i) + "\n");
            }
            out.close();
        } catch (IOException e) {
            System.out.println("error" + e);
        }
    }
    

    /**
     * 根据行号，列号，从list中获取信息，均从0开始计数 如行号，列号大于实际内容，则空指针异常
     * 
     * @param m
     * @param line
     * @param col
     * @return
     */
    private static Object getFromList(List m, int line, int col) {
        return ((List) (m.get(line))).get(col);
    }

    /**
     * @param startLine 从文件的第几行开始读取，如无特殊情况不需忽略文件开始的行，则默认为0即可
     * @return 返回的list包含子list，均从0开始计数
     * @throws IOException
     */
    private static List file2List(int startLine) throws IOException {
        List rtn = new ArrayList();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
        String data = "";
        int n = 0;
        while ((data = br.readLine()) != null) {
            if (data != null && !data.trim().equals("")) {// ignore null
                if (n >= startLine) {
                    String[] s = data.split(",");
                    if (s != null && s.length > 0) {
                        List line = new ArrayList();
                        for (int i = 0; i < s.length; i++) {
                            // System.out.println(s[i]);
                            line.add(s[i]);
                        }
                        rtn.add(line);
                    }
                }// end if startLine
                n++;
            }// end if null
        }// end while

        br.close();

        return rtn;
    }
    
}
