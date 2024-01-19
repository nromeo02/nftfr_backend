package org.nftfr.backend.controller.rest;
import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.persistence.model.User;
import org.nftfr.backend.utility.AuthToken;
import org.nftfr.backend.utility.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/payment")
public class PaymentMethodRest {
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();
    public record CreateBody(String address, int type){
        public PaymentMethod asPaymentMethod(User user) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setAddress(address);
            paymentMethod.setUser(user);
            paymentMethod.setType(type);
            return paymentMethod;
        }
    }

    @PutMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody CreateBody bodyParams, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        User user = DBManager.getInstance().getUserDao().findByUsername(authToken.username());
        if (user == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "User not found");

        if (!paymentMethodDao.add(bodyParams.asPaymentMethod(user)))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "The payment method is already registered");
    }

    @DeleteMapping("/delete/{address}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String address, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        PaymentMethod paymentMethod = paymentMethodDao.findByAddress(address);

        if (paymentMethod == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        if (!paymentMethod.getUser().getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "You don't have the permissions for this action");

        paymentMethodDao.delete(address);
    }

    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentMethod> get(HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        return paymentMethodDao.findByUsername(authToken.username());
    }
}
