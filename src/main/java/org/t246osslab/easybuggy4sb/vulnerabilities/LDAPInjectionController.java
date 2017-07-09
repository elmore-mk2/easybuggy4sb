package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.server.core.filtering.EntryFilteringCursor;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.filter.FilterParser;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.apache.directory.shared.ldap.message.AliasDerefMode;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;
import org.t246osslab.easybuggy4sb.core.dao.EmbeddedADS;

@Controller
public class LDAPInjectionController extends DefaultLoginController {

    private static final Logger log = LoggerFactory.getLogger(LDAPInjectionController.class);

    @Override
    @RequestMapping(value = "/ldapijc/login", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        req.setAttribute("note", msg.getMessage("msg.note.ldap.injection", null, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @RequestMapping(value = "/ldapijc/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        return super.doPost(mav, req, res, locale);
    }
    
    @Override
    protected boolean authUser(String username, String password) {

        if (StringUtils.isBlank(username) || username.length() < 5 || StringUtils.isBlank(password)
                || password.length() < 8) {
            return false;
        }
        
        ExprNode filter = null;
        EntryFilteringCursor cursor = null;
        try {
            filter = FilterParser.parse("(&(uid=" + username.trim() + ")(userPassword=" + password.trim() + "))");
            cursor = EmbeddedADS.getAdminSession().search(new LdapDN("ou=people,dc=t246osslab,dc=org"),
                    SearchScope.SUBTREE, filter, AliasDerefMode.NEVER_DEREF_ALIASES, null);
            if (cursor.available()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    log.error("Exception occurs: ", e);
                }
            }
        }
        return false;
    }
}