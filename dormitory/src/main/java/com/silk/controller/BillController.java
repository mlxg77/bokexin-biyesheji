package com.silk.controller;

import com.github.pagehelper.PageInfo;
import com.silk.entity.Bill;
import com.silk.entity.Repair;
import com.silk.entity.Room;
import com.silk.entity.User;
import com.silk.service.BillService;
import com.silk.service.BuildingService;
import com.silk.service.RoomService;
import com.silk.service.UserService;
import com.silk.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * @author LindaSilk
 * @date 2021年3月13日, 周六
 */
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    private BillService billService;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoomService roomService;

    @PostMapping("create")
    public Result create(@RequestBody Bill bill){
        int flag = billService.create(bill);
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @GetMapping("delete")
    public Result delete(String ids){
        int flag = billService.delete(ids);
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @PostMapping("update")
    public Result update(@RequestBody Bill bill){
        int flag = billService.update(bill);
        if(flag>0){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    @GetMapping("detail")
    public Bill detail(Integer id){
        return billService.detail(id);
    }

    @PostMapping("query")
    public Map<String,Object> query(@RequestBody  Bill bill){
        PageInfo<Bill> pageInfo = billService.query(bill);
        return Result.ok(pageInfo);
    }
    
    
    /*
     * 账单状态约定：未生成=0（学生已生成，等待宿管员推送）；已推送=1（待学生支付）；已支付=2
     */
    
    
    //  学生端：随机生成本人寝室当月的水电账单（水5元/吨，电1元/度，用量随机）
    @PostMapping("generate")
    public Result generate(@RequestBody Bill bill, HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的学生信息
    
        if (param.getUserType() == null || param.getUserType() != 0){
            return Result.fail("只有学生身份才能生成水电账单哦");
        }
    
        User student = userService.detail(param.getId());              // 从数据库获取该学生的最新信息
        Room room = roomService.detail(student.getRoomId());           // 该学生所在的寝室
        if (room == null){
            return Result.fail("您还没有分配宿舍房间，分配房间后再来生成账单吧");
        }
    
        // 未指定年月时默认生成当前月份的账单
        Calendar calendar = Calendar.getInstance();
        int billYear = (bill != null && bill.getBillYear() != null) ? bill.getBillYear() : calendar.get(Calendar.YEAR);
        int billMonth = (bill != null && bill.getBillMonth() != null) ? bill.getBillMonth() : calendar.get(Calendar.MONTH) + 1;
    
        // 同一寝室同一年同一月只能生成一张账单，防止重复生成
        Bill check = new Bill();
        check.setBillYear(billYear);
        check.setBillMonth(billMonth);
        check.setRoomId(room.getId());
        if (billService.count(check) > 0){
            return Result.fail(billYear + "年" + billMonth + "月的账单已经生成过啦，不用重复生成哦");
        }
    
        // 随机生成用量：用水1~10吨（保留1位小数），用电10~100度
        Random random = new Random();
        double waterUsed = Math.round((1 + random.nextDouble() * 9) * 10) / 10.0;
        double energyUsed = 10 + random.nextInt(91);
    
        // 按单价计算费用：水5元/吨，电1元/度（四舍五入保留两位小数）
        double waterFee = new BigDecimal(waterUsed * 5).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double energyFee = new BigDecimal(energyUsed * 1).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double totalFee = new BigDecimal(waterFee + energyFee).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    
        // 打包账单信息并插入数据库
        Bill newBill = new Bill();
        newBill.setBillYear(billYear);
        newBill.setBillMonth(billMonth);
        newBill.setBuildingId(room.getBuildingId());
        newBill.setRoomId(room.getId());
        newBill.setWaterUsed(waterUsed);
        newBill.setWaterFee(waterFee);
        newBill.setEnergyUsed(energyUsed);
        newBill.setEnergyFee(energyFee);
        newBill.setTotalFee(totalFee);
        newBill.setPaid(0);                                            // 0：未生成状态（等待宿管员推送）
    
        int flag = billService.create(newBill);
        if(flag>0){
            return Result.ok("账单生成成功，等宿管员推送后就能支付啦", newBill);
        }else{
            return Result.fail("账单生成失败了，请稍后再试");
        }
    }
    
    
    //  学生端：查询自己寝室的水电账单（只能看到自己寝室的）
    @PostMapping("query_my")
    public Map<String,Object> queryMy(@RequestBody Bill bill, HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的学生信息
    
        User student = userService.detail(param.getId());
        Room room = roomService.detail(student.getRoomId());
        if (room == null){                                             // 未分配房间时返回空列表
            return Result.ok(new PageInfo<Bill>(new ArrayList<Bill>()));
        }
    
        bill.setRoomId(room.getId());                                  // 强制限定为本人寝室，防止查询他人账单
        PageInfo<Bill> pageInfo = billService.query(bill);
        pageInfo.getList().forEach(entity->{
            entity.setBuilding(buildingService.detail(entity.getBuildingId()));
            entity.setRoom(roomService.detail(entity.getRoomId()));
        });
        return Result.ok(pageInfo);
    }
    
    
    //  宿管员端：查询自己管理的所有楼栋的账单
    @PostMapping("dorm_query")
    public Map<String,Object> dormQuery(@RequestBody Bill bill, HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的宿管员信息
    
        if (param.getUserType() == null || param.getUserType() == 0){   // 学生无权查看整栋账单
            return Result.ok(new PageInfo<Bill>(new ArrayList<Bill>()));
        }
    
        List<Integer> buildingIds = buildingService.queryDomersBuilding(param.getId());    // 该宿管员管理的所有楼栋
        if (buildingIds == null || buildingIds.size() == 0){            // 未管理任何楼栋时返回空列表
            return Result.ok(new PageInfo<Bill>(new ArrayList<Bill>()));
        }
    
        bill.setBuildingIds(buildingIds);
        PageInfo<Bill> pageInfo = billService.query(bill);
        pageInfo.getList().forEach(entity->{
            entity.setBuilding(buildingService.detail(entity.getBuildingId()));
            entity.setRoom(roomService.detail(entity.getRoomId()));
        });
        return Result.ok(pageInfo);
    }
    
    
    //  宿管员端：把未生成状态的账单推送给学生（状态0 -> 1）
    @PostMapping("push")
    public Result push(@RequestBody Bill bill, HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的宿管员信息
    
        if (param.getUserType() == null || param.getUserType() != 1){
            return Result.fail("只有宿管员才能推送账单哦");
        }
        if (bill.getId() == null){
            return Result.fail("请先选择要推送的账单");
        }
    
        Bill entity = billService.detail(bill.getId());
        if (entity == null){
            return Result.fail("没有找到这条账单，可能已被删除");
        }
        if (entity.getPaid() == null || entity.getPaid() != 0){
            return Result.fail("只有未生成状态的账单才能推送哦");
        }
    
        // 校验该账单所在楼栋是否由当前宿管员管理
        List<Integer> buildingIds = buildingService.queryDomersBuilding(param.getId());
        if (buildingIds == null || !buildingIds.contains(entity.getBuildingId())){
            return Result.fail("这条账单不属于您管理的楼栋，不能推送哦");
        }
    
        Bill up = new Bill();
        up.setId(entity.getId());
        up.setPaid(1);                                                  // 1：已推送，等待学生支付
        int flag = billService.updateSelective(up);
        if(flag>0){
            return Result.ok("推送成功，学生看到账单后就能支付啦");
        }else{
            return Result.fail();
        }
    }
    
    
    //  学生端：支付已推送的账单（状态1 -> 2）
    @PostMapping("pay")
    public Result pay(@RequestBody Bill bill, HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的学生信息
    
        if (param.getUserType() == null || param.getUserType() != 0){
            return Result.fail("只有学生身份才能支付账单哦");
        }
        if (bill.getId() == null){
            return Result.fail("请先选择要支付的账单");
        }
    
        Bill entity = billService.detail(bill.getId());
        if (entity == null){
            return Result.fail("没有找到这条账单，可能已被删除");
        }
    
        // 只能支付自己寝室的账单
        User student = userService.detail(param.getId());
        if (student.getRoomId() == null || !student.getRoomId().equals(entity.getRoomId())){
            return Result.fail("只能支付自己寝室的账单哦");
        }
        if (entity.getPaid() == null || entity.getPaid() != 1){
            return Result.fail("这条账单还没推送给您（或者已经付过啦），暂时不能支付哦");
        }
    
        Bill up = new Bill();
        up.setId(entity.getId());
        up.setPaid(2);                                                  // 2：学生已支付
        int flag = billService.updateSelective(up);
        if(flag>0){
            return Result.ok("支付成功，感谢配合！");
        }else{
            return Result.fail("支付失败了，请稍后再试");
        }
    }
    
    
    @GetMapping("/paid_info")
    public Result paidInfo(HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的宿舍管理员信息
        User domer = userService.detail(param.getId());                     // 从数据库获取该宿舍管理员的更多信息
        domer.setRoom(roomService.detail(domer.getRoomId()));               // 从数据库获取该宿舍管理员所在房间的信息

        Room room = domer.getRoom();                                        // 房间信息(该账号未绑定房间时为null)
        if (room == null) {                                                 // 未绑定房间则无法定位楼栋，避免空指针
            return Result.fail("当前账号未绑定宿舍房间，请先为其分配房间后再查询账单信息");
        }

        int buildingId = room.getBuildingId();                              // 房间所在楼栋的ID
        System.out.println(buildingId);

        /*
         * @silkTag 根据楼栋ID查询水电账单付款信息，随后以数组存入billPaidInfo数据域
         */
        List<Integer> paidInfo = new ArrayList<>();
        paidInfo.add(billService.notPaidAmount(buildingId));
        paidInfo.add(billService.havePaidAmount(buildingId));
        paidInfo.add(billService.delayPaidAmount(buildingId));
        domer.setBillPaidInfo(paidInfo);


        /*
         * @silkTag 根据楼栋ID查询用水量、水费、用电量、电费、总费用信息，随后以数组存入billUsedAndFeeInfo数据域
         */
        List<Double> usedAndFeeInfo = new ArrayList<>();

        // 将数据处理后四舍五入保留两位小数
        Double waterUsed = billService.totalWaterUsed(buildingId);
        BigDecimal bd1 = new BigDecimal(waterUsed);
        waterUsed = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        usedAndFeeInfo.add(waterUsed);

        Double waterFee = billService.totalWaterFee(buildingId);
        BigDecimal bd2 = new BigDecimal(waterFee);
        waterFee = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        usedAndFeeInfo.add(waterFee);

        Double energyUsed = billService.totalEnergyUsed(buildingId);
        BigDecimal bd3 = new BigDecimal(energyUsed);
        energyUsed = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        usedAndFeeInfo.add(energyUsed);

        Double energyFee = billService.totalEnergyFee(buildingId);
        BigDecimal bd4 = new BigDecimal(energyFee);
        energyFee = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        usedAndFeeInfo.add(energyFee);

        Double totalUsed = billService.totalFee(buildingId);
        BigDecimal bd5 = new BigDecimal(totalUsed);
        totalUsed = bd5.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        usedAndFeeInfo.add(totalUsed);

        domer.setBillUsedAndFeeInfo(usedAndFeeInfo);


        // 测试
        System.out.println("not paid / have paid / delay paid");
        System.out.println("----------");
        System.out.println(paidInfo);

        System.out.println("water used / water fee / energy used / energy fee / total fee");
        System.out.println("----------");
        System.out.println(usedAndFeeInfo);

        System.out.println(domer.getBillPaidInfo());
        System.out.println(domer.getBillUsedAndFeeInfo());

        return Result.ok(domer);
    }



    @GetMapping("/query_history_bill_info")
    public Result queryHistoryBillInfo(HttpServletRequest request){
        User param = (User)request.getAttribute("user");                // 获取发起当前操作的宿舍管理员信息
        User domer = userService.detail(param.getId());                     // 从数据库获取该宿舍管理员的更多信息
        domer.setRoom(roomService.detail(domer.getRoomId()));               // 从数据库获取该宿舍管理员所在房间的信息

        Room room = domer.getRoom();                                        // 房间信息(该账号未绑定房间时为null)
        if (room == null) {                                                 // 未绑定房间则无法定位楼栋，避免空指针
            return Result.fail("当前账号未绑定宿舍房间，请先为其分配房间后再查询账单信息");
        }

        int buildingId = room.getBuildingId();                              // 房间所在楼栋的ID
        System.out.println(buildingId);

        int latestYear = billService.queryLatestYear();
        int latestMonth = billService.queryLatestMonth();
        System.out.println("Latest Year&Month: " + latestYear + latestMonth);

        // 用于循环计数，六次而停（前端显示六个月的数据变化）
        int flag = 0;


        List<Double> historyWaterUsedList = new ArrayList<>();
        List<Double> historyWaterFeeList = new ArrayList<>();
        List<Double> historyEnergyUsedList = new ArrayList<>();
        List<Double> historyEnergyFeeList = new ArrayList<>();
        List<Double> historyTotalFeeList = new ArrayList<>();
        List<Integer> historyMonthList = new ArrayList<>();


        for(int i=latestMonth; i>0; i--){
            Double historyWaterUsed = billService.historyWaterUsed(buildingId, latestYear, i);
            BigDecimal bd1 = new BigDecimal(historyWaterUsed);
            historyWaterUsed = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            historyWaterUsedList.add(historyWaterUsed);

            Double historyWaterFee = billService.historyWaterFee(buildingId, latestYear, i);
            BigDecimal bd2 = new BigDecimal(historyWaterFee);
            historyWaterFee = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            historyWaterFeeList.add(historyWaterFee);

            Double historyEnergyUsed = billService.historyEnergyUsed(buildingId, latestYear, i);
            BigDecimal bd3 = new BigDecimal(historyEnergyUsed);
            historyEnergyUsed = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            historyEnergyUsedList.add(historyEnergyUsed);

            Double historyEnergyFee = billService.historyEnergyFee(buildingId, latestYear, i);
            BigDecimal bd4 = new BigDecimal(historyEnergyFee);
            historyEnergyFee = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            historyEnergyFeeList.add(historyEnergyFee);

            Double historyTotalUsed = billService.historyTotalUsed(buildingId, latestYear, i);
            BigDecimal db5 = new BigDecimal(historyTotalUsed);
            historyTotalUsed = db5.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            historyTotalFeeList.add(historyTotalUsed);

            historyMonthList.add(i);
            flag ++;
        }


        int newYear = latestYear - 1;
        int newMonth = 12;
        while(flag < 6){
            historyWaterUsedList.add(billService.historyWaterUsed(buildingId, newYear, newMonth));
            historyWaterFeeList.add(billService.historyWaterFee(buildingId, newYear, newMonth));
            historyEnergyUsedList.add(billService.historyEnergyUsed(buildingId, newYear, newMonth));
            historyEnergyFeeList.add(billService.historyEnergyFee(buildingId, newYear, newMonth));
            historyTotalFeeList.add(billService.historyTotalUsed(buildingId, newYear, newMonth));
            historyMonthList.add(newMonth);
            flag ++;
            newMonth --;
        }

        System.out.println("历史列表：");
        System.out.println(historyWaterUsedList);
        System.out.println(historyWaterFeeList);
        System.out.println(historyEnergyUsedList);
        System.out.println(historyEnergyFeeList);
        System.out.println(historyTotalFeeList);


        List<Object> finalList = new ArrayList<>();
        finalList.add(historyWaterUsedList);
        finalList.add(historyWaterFeeList);
        finalList.add(historyEnergyUsedList);
        finalList.add(historyEnergyFeeList);
        finalList.add(historyTotalFeeList);
        finalList.add(historyMonthList);
        System.out.println("汇总：");
        System.out.println(finalList);

        return Result.ok(finalList);
    }




    //    @PostMapping("/upload")
    @PostMapping("upload")
    @ResponseBody
    public Result excelImport(@RequestParam(value = "file") MultipartFile file){
        String fileName = file.getOriginalFilename();
        System.out.println(fileName);
        int result = 0;

        try {
            result = billService.addToDatabase(file);
        } catch (Exception e){
            e.printStackTrace();
        }

        if (result > 0){
            System.out.println("Upload result:");
            System.out.println(result);
            return Result.ok("恭喜，账单导入成功！");
        }else {
            return Result.fail("害！导入失败了...");
        }
    }

}
