package org.nftfr.backend.rest.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.nftfr.backend.persistence.DBManager;
import org.nftfr.backend.persistence.dao.PaymentMethodDao;
import org.nftfr.backend.persistence.model.PaymentMethod;
import org.nftfr.backend.rest.model.AuthToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/payment")
public class PaymentMethodRest {
    private final PaymentMethodDao paymentMethodDao = DBManager.getInstance().getPaymentMethodDao();
    private record CreateParams(String address, String username, int type, double balance){
        public PaymentMethod asPaymentMethod(){
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setAddress(address);
            paymentMethod.setUsername(username);
            paymentMethod.setType(type);
            paymentMethod.setBalance(balance);
            return paymentMethod;
        }
    }
    @PutMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPaymentMethod(@RequestBody CreateParams params, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        paymentMethodDao.add(params.asPaymentMethod());
    }
    @DeleteMapping("/delete/{address}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePaymentMethod(@PathVariable String address, HttpServletRequest request) {
        AuthToken authToken = AuthToken.fromRequest(request);
        if (authToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        PaymentMethod existingPaymentMethod = paymentMethodDao.findByAddress(address);

        if (existingPaymentMethod == null || !existingPaymentMethod.getUsername().equals(authToken.username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found");
        }
        paymentMethodDao.delete(address);
    }
}
