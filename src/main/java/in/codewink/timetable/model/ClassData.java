package in.codewink.timetable.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection="class-data")
public class ClassData {

    @Id
    private String id;

    private String semester;

    private boolean isOdd;

    private String totalSections;

    private List<TeacherSubjectData> teacherSubjectData;

    public ClassData() {
    }

    public ClassData(String semester, boolean isOdd, String totalSections, List<TeacherSubjectData> teacherSubjectData) {
        this.semester = semester;
        this.isOdd = isOdd;
        this.totalSections = totalSections;
        this.teacherSubjectData = teacherSubjectData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(String totalSections) {
        this.totalSections = totalSections;
    }

    public boolean isOdd() {
        return isOdd;
    }

    public void setOdd(boolean odd) {
        isOdd = odd;
    }

    public List<TeacherSubjectData> getTeacherSubjectData() {
        return teacherSubjectData;
    }

    public void setTeacherSubjectData(List<TeacherSubjectData> teacherSubjectData) {
        this.teacherSubjectData = teacherSubjectData;
    }
}
