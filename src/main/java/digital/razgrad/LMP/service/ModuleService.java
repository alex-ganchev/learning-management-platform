package digital.razgrad.LMP.service;

import digital.razgrad.LMP.auth.MyUserDetails;
import digital.razgrad.LMP.constant.UserRole;
import digital.razgrad.LMP.entity.Module;
import digital.razgrad.LMP.entity.Student;
import digital.razgrad.LMP.entity.User;
import digital.razgrad.LMP.hellper.EntityValidator;
import digital.razgrad.LMP.repository.CourseRepository;
import digital.razgrad.LMP.repository.ModuleRepository;
import digital.razgrad.LMP.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Service
public class ModuleService {
    private ModuleRepository moduleRepository;
    private CourseRepository courseRepository;
    private EntityValidator entityValidator;
    private UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private void setModuleRepository(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @Autowired
    private void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Autowired
    private void setEntityValidator(EntityValidator entityValidator) {
        this.entityValidator = entityValidator;
    }

    public String saveModule(Module module, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courses", courseRepository.findAll());
            model.addAttribute("module", module);
            return "/module/add";
        }
        redirectAttributes.addFlashAttribute("message", entityValidator.checkSaveSuccess(moduleRepository.save(module)));
        return "redirect:/module/list";
    }
    public String listAllModules(Model model, Authentication authentication) {
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        if(userDetails.getRole().equals(UserRole.ROLE_STUDENT)){
            model.addAttribute("moduleList",  moduleRepository.getModuleByStudentsId(userDetails.getId()));
        }else{
            model.addAttribute("moduleList",  moduleRepository.findAll());
        }
        return "/module/list";
    }

    public String editModule(@RequestParam Long id, Model model) {
        Optional<Module> optionalModule = moduleRepository.findById(id);
        if (optionalModule.isPresent()) {
            model.addAttribute("courseList", courseRepository.findAll());
            model.addAttribute("module", optionalModule.get());
            model.addAttribute("studentList", optionalModule.get().getCourse().getStudents());
            return "/module/edit";
        }
        return "redirect:/module/list";
    }

    public String updateModule(Module module, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseList", courseRepository.findAll());
            model.addAttribute("studentList", module.getCourse().getStudents());
            model.addAttribute("module", module);
            return "/module/edit";
        }
        redirectAttributes.addFlashAttribute("message", entityValidator.checkSaveSuccess(moduleRepository.save(module)));
        return "redirect:/module/list";
    }

    public String deleteModule(@RequestParam Long id, RedirectAttributes redirectAttributes, Model model) {
        if (validateSafeDeleteModule(id)) {
            moduleRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", entityValidator.checkDeleteSuccess(moduleRepository.existsById(id)));
            return "redirect:/module/list";
        }
        redirectAttributes.addFlashAttribute("message", "Модула има записани курсисти и/или добавени лекции. Не може да бъде изтрит!");
        return "redirect:/module/list";
    }

    public boolean validateSafeDeleteModule(Long id) {
        Optional<Module> optionalModule = moduleRepository.findById(id);
        if (optionalModule.isPresent() && optionalModule.get().getStudents().isEmpty() && optionalModule.get().getLectureSet().isEmpty()) {
            return true;
        }
        return false;
    }
}
