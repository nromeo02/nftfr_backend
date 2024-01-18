package org.nftfr.backend.rest.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.rest.model.AuthToken;
import org.nftfr.backend.rest.model.ClientErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/payment")
public class PaymentMethodRest {
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();
    public record CreateParams(String address, int type){
        public PaymentMethod asPaymentMethod(String username) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setAddress(address);
            paymentMethod.setUsername(username);
            paymentMethod.setType(type);
            return paymentMethod;
        }
    }

    @PutMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody CreateParams params, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        paymentMethodDao.add(params.asPaymentMethod(authToken.username()));
    }

    @DeleteMapping("/delete/{address}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String address, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        PaymentMethod paymentMethod = paymentMethodDao.findByAddress(address);

        if (paymentMethod == null)
            throw new ClientErrorException(HttpStatus.NOT_FOUND, "Payment method not found");

        if (!paymentMethod.getUsername().equals(authToken.username()))
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Invalid payment method");

        paymentMethodDao.delete(address);
    }

    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentMethod> get(HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        return paymentMethodDao.findByUsername(authToken.username());
    }
}
