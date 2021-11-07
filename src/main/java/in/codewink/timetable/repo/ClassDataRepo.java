package in.codewink.timetable.repo;

import in.codewink.timetable.model.ClassData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClassDataRepo extends MongoRepository<ClassData, String> {
    List<ClassData> findBySemester(String semester);
    List<ClassData> findByIsOdd(boolean isOdd);
}