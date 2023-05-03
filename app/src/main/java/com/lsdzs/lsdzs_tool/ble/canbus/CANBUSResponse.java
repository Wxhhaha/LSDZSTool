package com.lsdzs.lsdzs_tool.ble.canbus;

import com.blankj.utilcode.util.ArrayUtils;

public class CANBUSResponse {
    public static int[] dealResponse(byte[] value) {
        if (value[5] == (byte) 0xEF) {
            //控制器回复
            return dealControllerResponse(value);
        } else if (value[5] == (byte) 0xF4) {
            //bms回复
            return dealBMSResponse(value);
        }
        return new int[0];
    }

    /**
     * 处理电池返回信息
     * //剩余容量 满电容量 soc  soh  65282(ff02)
     * //电芯电压 65283（ff03） 65284(ff04) 65285(ff05) 65286(ff06)
     * //电压 电流 65290（ff0a）
     * //循环次数 充放电间隔 65296（ff10）
     *
     * @param value
     * @return
     */
    private static int[] dealBMSResponse(byte[] value) {
        int pgn = ((value[3] & 0xff) << 8) | (value[4] & 0xff);
        int len = value[6];
        byte[] data = ArrayUtils.subArray(value, 7, 7 + len);
        switch (pgn) {
            case 65282://容量等
                return dealBMScapacity(pgn, data, len);
            case 65283://电芯
                return dealCellTotal(pgn, data, len);
            case 65284://电芯
            case 65285:
            case 65286:
                return dealCell(pgn, data, len);
            case 65290://电压电流
                return dealVoltage(pgn, data, len);
            case 65296://循环次数
                return dealCycle(pgn, data, len);
        }
        return new int[0];
    }

    /**
     * 电池组记录数据
     *
     * @param pgn
     * @param data
     * @param len
     * @return
     */
    private static int[] dealCycle(int pgn, byte[] data, int len) {
        if (data == null || data.length < len) {
            return new int[0];
        }
        int[] cycle = new int[4];
        cycle[0] = pgn;
        cycle[1] = ((data[1] & 0xff) << 8) | (data[0] & 0xff);//循环次数
        cycle[2] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//充电时间间隔
        cycle[3] = ((data[5] & 0xff) << 8) | (data[4] & 0xff);//最大充电时间间隔
        return cycle;
    }

    private static int[] dealVoltage(int pgn, byte[] data, int len) {
        if (data == null || data.length < len) {
            return new int[0];
        }
        int[] vol = new int[3];
        vol[0] = pgn;
        vol[1] = ((data[1] & 0xff) << 8) | (data[0] & 0xff);//电池组电压
        vol[2] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//电池组电流
        return vol;
    }

    private static int[] dealCell(int pgn, byte[] data, int len) {
        if (data == null || data.length < len) {
            return new int[0];
        }
        int[] cells = new int[5];
        cells[0] = pgn;
        cells[1] = ((data[1] & 0xff) << 8) | (data[0] & 0xff);//电芯电压 1mV
        cells[2] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//电芯电压 1mV
        cells[3] = ((data[5] & 0xff) << 8) | (data[4] & 0xff);//电芯电压 1mV
        cells[4] = ((data[7] & 0xff) << 8) | (data[6] & 0xff);//电芯电压 1mV
        return cells;
    }

    private static int[] dealCellTotal(int pgn, byte[] data, int len) {
        if (data == null || data.length < len) {
            return new int[0];
        }
        int[] cells = new int[6];
        cells[0] = pgn;
        cells[1] = data[0];//电池组串数
        cells[2] = data[1];//电池组并数
        cells[3] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//电芯电压 1mV
        cells[4] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//电芯电压 1mV
        cells[5] = ((data[3] & 0xff) << 8) | (data[2] & 0xff);//电芯电压 1mV
        return cells;
    }

    /**
     * 电池组综合数据
     *
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealBMScapacity(int pgn, byte[] value, int len) {
        if (value == null || value.length < len) {
            return new int[0];
        }
        int[] capacity = new int[6];
        capacity[0] = pgn;
        capacity[1] = value[0];//健康状态
        capacity[2] = value[1];//剩余电量
        capacity[3] = ((value[3] & 0xff) << 8) | (value[2] & 0xff);//剩余容量 0.01Ah
        capacity[4] = ((value[5] & 0xff) << 8) | (value[4] & 0xff);//满电容量
        capacity[5] = ((value[7] & 0xff) << 8) | (value[6] & 0xff);//设计容量
        return capacity;
    }

    private static int[] dealControllerResponse(byte[] value) {
        int pgn = ((value[3] & 0xff) << 8) | (value[4] & 0xff);
        int len = value[6];
        byte[] data = ArrayUtils.subArray(value, 7, 7 + len);
        switch (pgn) {
            case 65026:
                //控制器实时参数
                return dealControllerSpeedData(pgn, data);
            case 65027:
                //电压电流温度
                return dealControllerVoltageData(pgn, data);
            case 65028:
                //总里程
                return dealControllerMileageData(pgn, data);
            case 65029:
                //控制器参数外发
                return dealControllerSetData(pgn, data);
            case 65030:
                //故障检测结果外发
                return dealCheckData(pgn, data);

        }
        return new int[0];
    }

    /**
     * 处理故障检测结果
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealCheckData(int pgn, byte[] value) {
        if (value == null) {
            return new int[0];
        }
        int[] resultData = new int[3];
        resultData[0] = pgn;
        resultData[1] = value[0];
        resultData[2] = value[1];
        return resultData;
    }

    /**
     * 处理外发控制器参数
     *
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealControllerSetData(int pgn, byte[] value) {
        if (value == null) {
            return new int[0];
        }
        int[] setData = new int[3];
        setData[0] = pgn;
        int addH, addL, valH, valL;
        CANBUSCmd.setKeys(value[4], value[5], value[6], value[7]);
        addL = (value[0] & 0xff) ^ CANBUSCmd.x2 ^ 0x9B;
        addH = (value[1] & 0xff) ^ CANBUSCmd.x1 ^ 0x7D;
        valL = (value[2] & 0xff) ^ CANBUSCmd.x4 ^ 0x3E;
        valH = (value[3] & 0xff) ^ CANBUSCmd.x3 ^ 0x54;
        if (((valH << 8) + valL) != 0xFFFF) {
            int address = (addH << 8) + addL;
            int valueData = (valH << 8) + valL;
            setData[1] = address;
            setData[2] = valueData;
            return setData;
        }
        return new int[0];
    }

    /**
     * 处理控制器电压电流等
     *
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealControllerVoltageData(int pgn, byte[] value) {
        if (value == null) {
            return new int[0];
        }
        int[] dataB = new int[7];
        dataB[0] = pgn;
        dataB[1] = ((value[1] & 0xff) << 8) | (value[0] & 0xff);//电池电压*10
        dataB[2] = ((value[3] & 0xff) << 8) | (value[2] & 0xff);//电池电流*10
        if ((value[4] & 0xff) == 255) {
            dataB[3] = 50;
        } else {
            dataB[3] = (value[6] & 0xff);//控制器温度+50
        }
        if ((value[5] & 0xff) == 255) {
            dataB[4] = 50;
        } else {
            dataB[4] = (value[7] & 0xff);//电机温度+50
        }
        dataB[5] = value[6] & 0xff;//实时功率百分比
        dataB[6] = value[7] & 0xff;//soc
        return dataB;
    }

    /**
     * 处理总里程
     *
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealControllerMileageData(int pgn, byte[] value) {
        if (value == null) {
            return new int[0];
        }
        int[] dataB = new int[2];
        dataB[0] = pgn;
        int data1 = value[3] & 0xff;//总里程2
        int data2 = value[4] & 0xff;//总里程1
        int data3 = value[5] & 0xff;//总里程0
        dataB[1] = data3 << 16 | (data2 << 8) | data1;//总里程
        return dataB;
    }

    /**
     * 处理速度等实时数据
     *
     * @param pgn
     * @param value
     * @return
     */
    private static int[] dealControllerSpeedData(int pgn, byte[] value) {
        if (value == null) {
            return new int[0];
        }
        int[] dataA = new int[12];
        dataA[0] = pgn;
        dataA[1] = value[0] & 0x0f;//助力档位
        dataA[2] = (value[0] >> 4) & 0x01;//6km推行
        dataA[3] = (value[0] >> 5) & 0x01;//大灯
        dataA[4] = (value[0] >> 6) & 0x01;//刹车状态
        dataA[5] = (value[0] >> 7) & 0x01;//电机运转

        int speedH, speedL;
        speedH = value[1] & 0x0f;
        speedL = value[2] & 0xff;
        dataA[6] = (speedH << 8) + speedL;//速度*10

        dataA[7] = value[3] & 0xff;//踏频
        dataA[8] = value[4] & 0xff;//脚踏力矩
        dataA[9] = (value[5] & 0xff) << 8 | (value[6] & 0xff);//错误代码
        dataA[10] = (value[1] >> 4) & 0x01;//仪表屏幕有显示
        dataA[11] = (value[7] >> 1) & 0x01;//电源键状态
        return dataA;
    }
}
