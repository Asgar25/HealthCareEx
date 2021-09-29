package in.nareshit.raghu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import in.nareshit.raghu.entity.Doctor;
import in.nareshit.raghu.exception.DoctorNotFoundException;
import in.nareshit.raghu.service.IDoctorService;
import in.nareshit.raghu.service.ISpecializationService;
import in.nareshit.raghu.util.MyMailUtil;

@Controller
@RequestMapping("/doctor")
public class DoctorController {
	
	@Autowired
	private MyMailUtil mailUtil; 
	
	@Autowired
	private IDoctorService service;
	
	@Autowired
	private ISpecializationService specializationService;
	
	private void createDyanamicUi(Model model) {
		model.addAttribute("specializations", specializationService.getSpecIdAndName());
	}

	//1. show Register page
	@GetMapping("/register")
	public String showReg(
			@RequestParam(value = "message",required = false)String message,
			Model model
			) 
	{
		model.addAttribute("message", message);
		createDyanamicUi(model);
		return "DoctorRegister";
	}
	
	//2. save on submit
	@PostMapping("/save")
	public String save(
			@ModelAttribute Doctor doctor,
			RedirectAttributes attributes
			)
	{
		Long id = service.saveDoctor(doctor);
		String message = "Doctor ("+id+") is created";
		attributes.addAttribute("message", message);
		if(id!=null) {
			new Thread(new Runnable() {
				public void run() {
					mailUtil.send(
							doctor.getEmail(), 
							"SUCCESS", 
							message,
							new ClassPathResource("/static/myres/sample.pdf"));
				}
			}).start();
		}
		return "redirect:register";
	}
	
	//3. display data
	@GetMapping("/all")
	public String display(
			@RequestParam(value = "message",required = false)String message,
			Model model
			) 
	{
		List<Doctor>  list = service.getAllDoctors();
		model.addAttribute("list", list);
		model.addAttribute("message", message);
		return "DoctorData";
	}
	
	//4. delete by id
	@GetMapping("/delete")
	public String delete(
			@RequestParam("id")Long id,
			RedirectAttributes attributes
			)
	{
		String message = null;
		try {
			service.removeDoctor(id);
			message = "Doctor removed";
		} catch (DoctorNotFoundException e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		attributes.addAttribute("message", message);
		return "redirect:all";
	}
	
	//5. show edit
	@GetMapping("/edit")
	public String edit(
			@RequestParam("id")Long id,
			Model model,
			RedirectAttributes attributes
			)
	{
		String page = null;
		try {
			Doctor doc = service.getOneDoctor(id);
			model.addAttribute("doctor", doc);
			createDyanamicUi(model);
			page = "DoctorEdit";
		} catch (DoctorNotFoundException e) {
			e.printStackTrace();
			attributes.addAttribute("message", e.getMessage());
			page = "redirect:all";
		}
		return page;
	}
	
	
	//6. do update
	@PostMapping("/update")
	public String doUpdate(
			@ModelAttribute Doctor doctor,
			RedirectAttributes attributes
			) 
	{
		service.updateDoctor(doctor);
		attributes.addAttribute("message", doctor.getId()+", updated!");
		return "redirect:all";
	}
	
	//7. email and mobile duplicate validations (AJAX)
	
	//8. excel export
	
}
