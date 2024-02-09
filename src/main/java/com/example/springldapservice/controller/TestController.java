package com.example.springldapservice.controller;

import com.example.springldapservice.ldap.service.MyLdapService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TestController {

    private final MyLdapService myLdapService;
    @GetMapping("/hello")
    String sayHello() {
        return "Hello";
    }

    @GetMapping("/add-user")
    String adduser() throws Exception {
        String data = "dn: uid=kasya,ou=People,dc=keycloak,dc=org\n" +
                "uid: kasya\n" +
                "mail: kasya@keycloak.org\n" +
                "postalcode: 88441\n" +
                "cn: Asya\n" +
                "sn: Kasevna\n" +
                "objectclass: top\n" +
                "objectclass: person\n" +
                "objectclass: organizationalPerson\n" +
                "objectclass: inetOrgPerson\n" +
                "userpassword: password";
        myLdapService.addUser(data);
        return "success";
    }
}
