import java.math.BigDecimal;
/**
 * 功能:提供坐标偏移公共类
 * 
 * @author wangkai
 * @date 2017/08/17
 * 
 */
public class CoordinateUtil {
    //
    // Krasovsky 1940
    //
    // a = 6378245.0, 1/f = 298.3
    // b = a * (1 - f)
    // ee = (a^2 - b^2) / a^2;
    static double a = 6378245.0;
    static double ee = 0.00669342162296594323;
    static double pi = 3.14159265358979324;
    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    
    /**
     * GCJ02(国家局、高德、谷歌)转换为百度坐标
     * @param double x,double y
     * @return double[2] xy
     */
    public static double[] bd_encrypt(double x,double y)
    {
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double[] xy = new double[2];
        xy[0] = new BigDecimal(z * Math.cos(theta) + 0.0065).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        xy[1] = new BigDecimal(z * Math.sin(theta) + 0.006).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return xy;
    }
    
    /**
     * 百度坐标转换为GCJ02(国家局、高德、谷歌)
     * @param double x,double y
     * @return double[2] xy
     */
    public static double[] bd_decrypt(double x,double y)
    {
        x = x - 0.0065;
        y = y - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double[] xy = new double[2];
        xy[0] = new BigDecimal(z * Math.cos(theta)).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        xy[1] = new BigDecimal(z * Math.sin(theta)).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        return xy;
    }

    
    /**
     * 原始坐标(WGS84)转换为GCJ02(国家局、高德、谷歌)
     * @param double x,double y
     * @return double[2] xy
     */
    public static double[] wgsToGcj(double x,double y){
        double[] gcjLoc = new double[2];
        if (outOfChina(x, y))
        {
            gcjLoc[0] = x;
            gcjLoc[1] = y;
            return gcjLoc;
        }
        double dLat = transLat(x - 105.0, y - 35.0);
        double dLon = transLon(x - 105.0, y - 35.0);
        double radLat = y / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        gcjLoc[1] = new BigDecimal(y + dLat).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        gcjLoc[0] = new BigDecimal(x + dLon).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

        return gcjLoc;
    }
    
    /**
     * GCJ02(国家局、高德、谷歌)转换为原始坐标(WGS84)
     * @param double x,double y
     * @return double[2] xy
     */
    public static double[] gcjToWgs(double x,double y)
    {
        double[] wgLoc = new double[2];
        double wgX = x, wgY = y;
        double dX,dY;
        double[] currGcLoc;
        int maxCount = 100;
        int count = 0;
        while (true) {
            currGcLoc = wgsToGcj(wgX,wgY);
            dX = x - currGcLoc[0];
            dY = y - currGcLoc[1];
            if (Math.abs(dY) < 1e-5 && Math.abs(dX) < 1e-5) {  // 1e-6 ~ centimeter level accuracy
              // Result of experiment:
              //   Most of the time 2 iterations would be enough for an 1e-8 accuracy (milimeter level).
              //
                wgLoc[0] = new BigDecimal(wgX).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                wgLoc[1] = new BigDecimal(wgY).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                break;
            }
            wgX += dX;
            wgY += dY;
            if(count>maxCount){//超过100次计算未满足条件，返回0
                wgLoc[0] = 0;
                wgLoc[1] = 0;
                break;
            }
            count++;
        }
        return wgLoc;
    }
    /**
     * 
     * 方法描述: 原始坐标转百度
     * 
     * @param x
     * @param y
     * @return double[]
     * @author wangkai 2017年8月17日
     *
     * 修改描述：
     * @modifier
     */
    public static double[] wgsToBd(double x,double y){
        double[] xy=wgsToGcj(x, y);//转成国家局
        double[] bdLoc = bd_encrypt(xy[0], xy[1]);//转成百度
        return bdLoc;
    }
    /**
     * 
     * 方法描述: 百度转原始坐标
     * 
     * @param x
     * @param y
     * @return double[]
     * @author wangkai 2017年8月17日
     *
     * 修改描述：
     * @modifier
     */
    public static double[] bdToWgs(double x,double y){
        double[] xy=bd_decrypt(x, y);//转成国家局
        double[] wgsLoc = gcjToWgs(xy[0], xy[1]);//转成原始坐标(WGS84)
        return wgsLoc;
    }
    
    
    private static double transLat(double x,double y){
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x:-x);
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
            
    private static double transLon(double x,double y){
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x:-x);
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }       
    //查看是否不在国内
    private static boolean outOfChina(double x,double y){
        if (x < 72.004 || x > 137.8347)
            return true;
        if (y < 0.8293 || y > 55.8271)
            return true;
        return false;
    }
}
