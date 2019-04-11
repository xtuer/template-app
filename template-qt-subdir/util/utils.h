/*
  Copyright ? 2015 Hasan Yavuz ?zderya

  This file is part of serialplot.

  serialplot is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  serialplot is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with serialplot.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifndef UTILS_H
#define UTILS_H
#include <QtCore>
#include <QThread>
#include <QTimer>
#include "QString"

#define FONTSIZE      10
#define AsixTimeAngle 0 //-35.00

#define BAOHEP2T(x) (-0.000000006*(x)*(x)*(x)*(x) + 0.000007572*(x)*(x)*(x)  \
                     -0.003475893*(x)*(x) + 0.811184915*(x) + 46.823092121 + 0.2)


#define BAOHET2P(x) (0.000001339*(x)*(x)*(x)*(x) - 0.000124849*(x)*(x)*(x)  \
                     +0.010772546*(x)*(x) - 0.155154206*(x) + 0.000010000)

enum {TYPE_HSENSOR = 'H',
      TYPE_TSENSOR = 'T',
      TYPE_PSENSOR = 'P',
      TYPE_INVALID = '0'};

enum {PRIVILEGE_NULL = 0,
      PRIVILEGE_LOW = 1,
      PRIVILEGE_MID = 2,
      PRIVILEGE_HIGH= 3};

enum {VERIFY_NULLSS = 0,
      VERIFY_MIEJUN = 1,
      VERIFY_XIAODU = 2,
      VERIFY_WAILAI = 3 };

enum
{
    SENSOR_UNKNOWN = 0x00,
    SENSOR_PRO3,
    SENSOR_PRO5
};

// credits: peppe@stackoverflow [http://stackoverflow.com/a/16795664/432492]
template<typename... Args> struct SELECT {
    template<typename C, typename R>
    static constexpr auto OVERLOAD_OF( R (C::*pmf)(Args...) ) -> decltype(pmf) {
        return pmf;
    }
};

/******************************************************************************/
struct infoVerify
{
    quint16  magic;                             //0x4455
    quint16  version;                           //0x0100
    QString  company;                           //公司名称
    QString  department;                        //部门名称
    QString  director;                          //责任人
    QString  address;                           //详细地址
    QString  phone;                             //电话号码
    QString  deviceProvider;                    //设备厂家
    QString  deviceType;                        //设备型号
    QString  deviceSerial;                      //设备编号
    QString  T4Serial;                          //4温度通道记录器序列号
    QString  T3P1Serial;                        //3温1压记录器序列号
    double   tempKill;                          //灭菌设定温度
    quint32  timeKill;                          //灭菌设定时间
    quint32  timeExpose;                        //设定暴露时间
    quint32  volume;                            //蒸汽灭菌锅容量
    quint32  fuzai;                             //空载/小负载/满负载温度
    quint32 killTimeEnd;                        //实际灭菌的结束时间
    quint32 killTimeStartMin;                   //开始灭菌最早时间
    quint32 killTimeStartMax;                   //开始灭菌最晚时间
    double   Temp8junyun;                       //8路的温度均匀性，小于2度合格
    quint32  Temp8junyunTime;                   //8路的温度均匀性suozaishijian
    double   TempLargest;                       //维持期间的最高温度(小无7;满有7)
    double   TempSmallest;                      //维持期间的最低温度(小无7;满有7)
    double   TempAverage;                       //维持期间的平均温度(小无7;满有7)
    int      TempLargestNumb;                   //维持期间最高通道号(小无7;满有7)
    int      TempSmallestNumb;                  //维持期间最低通道号(小无7;满有7)
    quint32  TempLargestTime;                   //维持时间内最高时间(小无7;满有7)
    quint32  TempSmallestTime;                  //维持时间内最低时间(小无7;满有7)
    quint32  killTimePressAve;                  //灭菌时间内压力均值
    double   killTimeP2TAve;                    //灭菌时间内理论温度均值
    double   F0;                                //灭菌时间内F0均值
    quint32  timePingheng;    //平衡时间(<=800l: 15S, >800l: 30S)
    quint32  timeWeichi;      //维持时间(121:>15min,126:>10min, 134:>5min)
    bool     tempGap1Valid;   //60秒之前测量点1与4的最大温度差和个性(<5度)
    double   tempGap1;        //60秒之前测量点1与4的最大温度差(<5度)
    bool     tempGap2Valid;   //60秒之后测量点1与4的最大温度差(<2度)
    double   tempGap2;        //60秒之后测量点1与4的最大温度差(<5度)
    bool     temps567Valid;   //包含通道5通道6通道7的温度是否在范围之内
    double   temp567AtPingEnd[3];
};

/******************************************************************************/
struct infoSterilize
{
    quint16  magic;                              //0x4455
    quint16  version;                            //0x0100
    QString  company;                            //公司名称
    QString  department;                         //部门名称
    QString  director;                           //责任人
    QString  address;                            //详细地址
    QString  phone;                              //电话号码
    QString  deviceProvider;                     //设备厂家
    QString  deviceType;                         //设备型号
    QString  deviceSerial;                       //设备编号
    QString  T4Serial;                           //4温度通道记录器序列号
    QString  T3P1Serial;                         //3温1压记录器序列号
    double   tempXiaodu;                         //消毒设定温度
    quint32  timeXiaodu;                         //消毒设定时间
    quint32  type;                               //消毒设定类型
    QString  channelLocation[7];                 //记录器通道的位置
    double  xdA0[7];                             //消毒过程中的A0值
    quint32 xdEveryTimeStart[7];                 //每一个通道消毒开始时间
    quint32 xdEveryTimeEnd[7];                   //每一个通道消毒结束时间
    double  TempEveryLargest[7];                 //每个通道消毒期间的最高温度
    double  timeStartXiaoduMinus5;               //任意一个温度加5大于消毒温度的时刻
    double  timeEndXiaoduMinus5;                 //任意一个温度加5大于消毒温度的时刻
};

/******************************************************************************/
#define KILL_TEMP_COUNT 8
struct infoInstrument
{
    quint16  magic;                              //0x4455
    quint16  version;                            //0x0100
    QString  company;                            //公司名称
    QString  department;                         //部门名称
    QString  director;                           //责任人
    QString  deviceProvider;                     //灭菌器厂家
    QString  deviceType;                         //灭菌器型号
    QString  deviceSerial;                       //灭菌器编号
    QString  instruProvider;                     //器械厂家
    QString  instruName;                         //器械名称
    QString  T4Serial;                           //4温度通道记录器序列号
    QString  T3P1Serial;                         //3温1压记录器序列号
    double   tempKill;                           //灭菌设定温度
    quint32  timeKill;                           //灭菌设定时间
    QString  channelLocation[KILL_TEMP_COUNT];                 //记录器通道的位置
    quint32  killTimeStart[KILL_TEMP_COUNT];                   //实际灭菌开始的时间
    quint32  killTimeEnd[KILL_TEMP_COUNT];                     //实际灭菌的结束时间
    double   TempLargest[KILL_TEMP_COUNT];                     //灭菌期间的最高温度
    double   TempSmallest[KILL_TEMP_COUNT];                    //灭菌期间的最低温度
    double   TempAverage[KILL_TEMP_COUNT];                     //灭菌器件的平均温度
};

/******************************************************************************/
struct infoCommon
{
    QString  company;                            //公  司  名  称
    QString  department;                         //部  门  名  称
    QString  director;                           //责    任    人
    QString  address;                            //详  细  地  址
    QString  phone;                              //电  话  号  码
    QString  deviceName;                         //设  备  名  称
    QString  deviceString;                       //设  备  型  号
    QString  deviceSerial;                       //设  备  编  号
    QString  deviceProvider;                     //设  备  厂  家
    double   tempSetting;                        //灭菌 设 定 温度
    quint32  timeSetting;                        //灭菌 设 定 时间
    bool     buDianEnable;                       //是否使用布点图
    quint32  deviceType;                         //灭菌设备所属的类型
    bool     miejunTimeSpanFlag;                 //灭菌时间段参数输出
    bool     miejunKillTimeFlag;                 //是否输出灭菌测量时间
    bool     miejunKillAveFlag;                  //是否输出灭菌测量均值
    bool     miejunKillVariFlag;                 //是否输出灭菌测量方差
    bool     miejunKillTempMaxFlag;              //是否输出灭菌最大温度
    bool     miejunKillTempMinFlag;              //是否输出灭菌最小温度
    bool     miejunKillBdFlag;                   //是否输出灭菌 波动度
    bool     miejunKillF0Flag;                   //是否输出灭菌测量F0值
    bool     miejunKillA0Flag;                   //是否输出灭菌测量A0值
    bool     reportDataDetail;                   //是否输出统计表格报告
    bool     reportF0Detail;                     //是否输出温度点F0报告
    bool     reportAnalysis;                     //是否输出综合分析报告
    bool     reportAuditTrail;                   //是否输出审计追踪报告
};

/******************************************************************************/
typedef struct struct_Calc_Bias_th
{
    int    pointNum;          //0~5
    float  point[5];          //points  0~4
    float  bias[5];           //calebration 0~5
}STRU_CALC_TH;

typedef struct struct_Calc_Bias_p
{
    float  tempValue;         //temperature
    int    pointNum;          //0~5
    float  point[5];          //points  0~4
    float  bias[5];           //calebration 0~5
}STRU_CALC_P;

//struct of temperature correction coefficient
//T=Tmx+A*(Tm-Tmx)-dT
//Tmx: result from ADC before being calibrated
struct coeffTH {
    float Tm;       //measured value at TREF, Tm=TREF+dT
    float dT;       //offset value at TREF,
    float A;        //
};

struct coeffP {
    float Pm;       //measured value at TREF, Tm=TREF+dT
    float dP;       //offset value at TREF,
    float A;        //
};

/******************************************************************************/
/*****sensor's working parametes***********************************************/
/******************************************************************************/
struct infoWorkingSetting
{
    QString type;
    quint32 serial;
    char    user[11];
    quint8  status;
    quint8  real;
    quint32 start;
    quint32 end;
    quint32 current;
    quint32 inteval;
    quint32 num;
    float   voltage;
    quint32 powerOn;
    quint32 calcTime;
    char    version[30];
    quint32 highTempNum;
};

struct history_stru
{
    QString name;                                //用户名
    QString operation;                           //什么操作
    QString time;                                //操作时间
};
#endif // UTILS_H
