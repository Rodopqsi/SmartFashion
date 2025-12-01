package com.smarthfashion.admin.controller;

import com.smarthfashion.admin.domain.AppUser;
import com.smarthfashion.admin.repository.AppUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        List<AppUser> users = (q == null || q.isBlank())
            ? appUserRepository.findAllByOrderByFechaRegistroDesc()
            : appUserRepository.findByEmailContainingIgnoreCaseOrNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrderByFechaRegistroDesc(q, q, q);
        model.addAttribute("users", users);
        model.addAttribute("q", q == null ? "" : q);
        return "users/list";
    }

    @PostMapping("/{id}/block")
    public String block(@PathVariable("id") Long id) {
        AppUser u = appUserRepository.findById(id).orElseThrow();
        u.setBloqueado(true);
        appUserRepository.save(u);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unblock")
    public String unblock(@PathVariable("id") Long id) {
        AppUser u = appUserRepository.findById(id).orElseThrow();
        u.setBloqueado(false);
        appUserRepository.save(u);
        return "redirect:/admin/users";
    }
}
