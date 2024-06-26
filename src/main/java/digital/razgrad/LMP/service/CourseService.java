package digital.razgrad.LMP.service;

import digital.razgrad.LMP.constant.CourseStatus;
import digital.razgrad.LMP.constant.CourseType;
import digital.razgrad.LMP.constant.UserRole;
import digital.razgrad.LMP.entity.Course;
import digital.razgrad.LMP.hellper.EntityValidator;
import digital.razgrad.LMP.repository.CourseRepository;
import digital.razgrad.LMP.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;


@Service
public class CourseService {
    private CourseRepository courseRepository;
    private EntityValidator entityValidator;
    private UserRepository userRepository;
    @Autowired
    private void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    @Autowired
    private void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }
    @Autowired
    private void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String addCourse(Model model) {
        model.addAttribute("typeList", CourseType.values());
        model.addAttribute("course", new Course());
        model.addAttribute("teacherList", userRepository.getUserByUserRole(UserRole.ROLE_TEACHER));
        return "/course/add";
    }

    public String saveCourse(Course course, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("typeList", CourseType.values());
            model.addAttribute("teacherList", userRepository.getUserByUserRole(UserRole.ROLE_TEACHER));
            return "/course/add";
        }
        course.setStatus(CourseStatus.UPCOMING);
        redirectAttributes.addFlashAttribute("message", entityValidator.checkSaveSuccess(courseRepository.save(course)));
        return "redirect:/course/list";
    }

    public String editCourse(@RequestParam Long id, Model model) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isPresent()) {
            model.addAttribute("course", optionalCourse.get());
            model.addAttribute("typeList", CourseType.values());
            model.addAttribute("statusList", CourseStatus.values());
            model.addAttribute("teacherList", userRepository.getUserByUserRole(UserRole.ROLE_TEACHER));
            return "/course/edit";
        }
        return "redirect:/course/list";
    }

    public String updateCourse(Course course, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("typeList", CourseType.values());
            model.addAttribute("statusList", CourseStatus.values());
            model.addAttribute("teacherList", userRepository.getUserByUserRole(UserRole.ROLE_TEACHER));
            return "/course/edit";
        }

        redirectAttributes.addFlashAttribute("message", entityValidator.checkSaveSuccess(courseRepository.save(course)));
        return "redirect:/course/list";
    }

    public String deleteCourse(@RequestParam Long id, RedirectAttributes redirectAttributes, Model model) {
        if (validateSafeDeleteCourse(id)) {
            courseRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", entityValidator.checkDeleteSuccess(courseRepository.existsById(id)));
            return "redirect:/course/list";
        }
        redirectAttributes.addFlashAttribute("message", "Курса има добавени лектори, записани курсисти и/или добавени модули. Не може да бъде изтрит!");
        return "redirect:/course/list";
    }

    public boolean validateSafeDeleteCourse(Long id) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isPresent() && optionalCourse.get().getModuleSet().isEmpty() && optionalCourse.get().getTeachers().isEmpty() && optionalCourse.get().getStudents().isEmpty()) {
            return true;
        }
        return false;
    }

}
