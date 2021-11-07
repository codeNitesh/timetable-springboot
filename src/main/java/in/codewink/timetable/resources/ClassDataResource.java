package in.codewink.timetable.resources;

import in.codewink.timetable.model.ClassData;
import in.codewink.timetable.model.TeacherSubjectData;
import in.codewink.timetable.repo.ClassDataRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ClassDataResource {

    @Autowired
    ClassDataRepo classDataRepo;

    // Create a new class
    @PostMapping("/class-data")
    public ResponseEntity<ClassData> createTutorial(@RequestBody ClassData classData) {
        try {
            ClassData _classData = classDataRepo.save(new ClassData(classData.getSemester(), classData.isOdd(), classData.getTotalSections(), classData.getTeacherSubjectData()));
            return new ResponseEntity<>(_classData, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // get all classes
    @GetMapping("/class-data")
    public ResponseEntity<List<ClassData>> getAllTutorials(@RequestParam(required = false) String semester) {
        try {
            List<ClassData> classData = new ArrayList<ClassData>();

            if (semester == null)
                classDataRepo.findAll().forEach(classData::add);
            else
                classDataRepo.findBySemester(semester).forEach(classData::add);

            if (classData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(classData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // get full timetable by even or odd semester
    @GetMapping("/timetable")
    public ResponseEntity<Object> getTimeTable(@RequestParam Boolean odd) {
        try {

            List<ClassData> classData = classDataRepo.findByIsOdd(odd);
            // Main Logic

            int NUM_LECTURES = 6;
            int DAYS = 6;
            int CLASSES = classData.size();


            Map<String, Map<Integer, Map>> timetable = new HashMap<>();

            for (int currentSem = 0; currentSem < CLASSES; currentSem++) {
                int teacherStart = 1;
                ClassData currentClassData = classData.get(currentSem);
                Map<Integer, Map> sectionWiseList = new HashMap<>();
                for (int section = 1; section <= Integer.parseInt(currentClassData.getTotalSections()); section++) {

                    Map<String, ArrayList> allDaysList = new HashMap<>();
                    for (int d = 1; d <= DAYS; d++) {
                        ArrayList<String> day = new ArrayList<>();
                        for (int lecture = 1; lecture <= NUM_LECTURES; lecture++) {
                            int numOfTeachers = currentClassData.getTeacherSubjectData().size();
                            TeacherSubjectData temp = (TeacherSubjectData) currentClassData.getTeacherSubjectData().get((teacherStart - 1) % numOfTeachers);
                            day.add(temp.getTeacherName());
                            teacherStart++;
                        }

                        allDaysList.put(generateDay(d), day);
                        teacherStart++;
                    }
                    teacherStart++;
                    sectionWiseList.put(section, allDaysList);
                }

                timetable.put(currentClassData.getSemester(), sectionWiseList);
            }
            generateExcel(timetable);
            if (classData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(timetable, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/class-data")
    public ResponseEntity<HttpStatus> deleteAllClassData() {
        try {
            classDataRepo.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public void generateExcel(Map<String, Map<Integer, Map>> timetable) {
        try{
            //Create workbook in .xlsx format
            Workbook workbook = new XSSFWorkbook();
            //For .xsl workbooks use new HSSFWorkbook();
            //Create Sheet
            Sheet sh = workbook.createSheet("TimeTable");
            //Create top row with column headings
            String[] columnHeadings = {"Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};            //We want to make it bold with a foreground color.
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short)12);
            headerFont.setColor(IndexedColors.BLACK.index);
            //Create a CellStyle with the font
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
            //Create the header row
            Row headerRow = sh.createRow(0);
            for(int i=0;i<columnHeadings.length;i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnHeadings[i]);
                cell.setCellStyle(headerStyle);
            }
            //Freeze Header Row
            sh.createFreezePane(0, 1);

            int rownum =1;

            Set semSet= timetable.entrySet();//Converting to Set so that we can traverse
            Iterator itr=semSet.iterator();
            while(itr.hasNext()){
                //Converting to Map.Entry so that we can get key and value separately
                Map.Entry entry=(Map.Entry)itr.next();

                Row row = sh.createRow(rownum++);
                row.createCell(0).setCellValue(entry.getKey().toString()); // sem

                HashMap batchSet1 = (HashMap) entry.getValue();
                Set batchSet = batchSet1.entrySet();
                Iterator batchItr = batchSet.iterator();
                while (batchItr.hasNext()){
                    Map.Entry batchEntry = (Map.Entry) batchItr.next();
                    row = sh.createRow(rownum++);
                    row.createCell(0).setCellValue(batchEntry.getKey().toString()); // batch

                    HashMap daySet1 = (HashMap) batchEntry.getValue();
                    Set daySet = daySet1.entrySet();
                    Iterator dayItr = daySet.iterator();
                    while (dayItr.hasNext()){
                        Map.Entry dayEntry = (Map.Entry) dayItr.next();
                        row = sh.createRow(rownum++);
                        row.createCell(0).setCellValue(dayEntry.getKey().toString()); // Monday

                        ArrayList<String> mainData = (ArrayList<String>) dayEntry.getValue();
                        row = sh.createRow(rownum++);

                        for (int timeslots = 0; timeslots < mainData.size(); timeslots++) {
                            row.createCell(timeslots).setCellValue(mainData.get(timeslots));
                        }
                    }
                }
            }

//            //Group and collapse rows
//            int noOfRows = sh.getLastRowNum();
//            sh.groupRow(1, noOfRows);
//            sh.setRowGroupCollapsed(1, true);

            //Autosize columns
            for(int i=0;i<columnHeadings.length;i++) {
                sh.autoSizeColumn(i);
            }
            //Write the output to file
            String home = System.getProperty("user.home");
            FileOutputStream fileOut = new FileOutputStream(home + File.separator + "desktop" + File.separator + "timetable.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            System.out.println("Completed");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String generateDay(int day){
        String dayy = null;
        switch (day) {
            case 1:
                dayy = "Monday";
                break;
            case 2:
                dayy = "Tuesday";
                break;
            case 3:
                dayy = "Wednesday";
                break;
            case 4:
                dayy = "Thursday";
                break;
            case 5:
                dayy = "Friday";
                break;
            case 6:
                dayy = "Saturday";
                break;
        }

        return dayy;
    }


}
