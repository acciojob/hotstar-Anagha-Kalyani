package com.driver.services;


import com.driver.EntryDto.SubscriptionEntryDto;
import com.driver.model.Subscription;
import com.driver.model.SubscriptionType;
import com.driver.model.User;
import com.driver.repository.SubscriptionRepository;
import com.driver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    UserRepository userRepository;

    public Integer buySubscription(SubscriptionEntryDto subscriptionEntryDto){

        //Save The subscription Object into the Db and return the total Amount that user has to pay

        User customer = userRepository.findById(subscriptionEntryDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = new Subscription();
        subscription.setSubscriptionType(subscriptionEntryDto.getSubscriptionType());
        subscription.setNoOfScreensSubscribed(subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setStartSubscriptionDate(new Date());

        // Calculate total amount to pay based on subscription type and number of screens
        int totalAmountToPay = calculateTotalAmountToPay(subscriptionEntryDto.getSubscriptionType(),
                subscriptionEntryDto.getNoOfScreensRequired());
        subscription.setTotalAmountPaid(totalAmountToPay);

        // Set subscription for the customer
        customer.setSubscription(subscription);

        // Save the subscription
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return totalAmountToPay;
    }


    private int calculateTotalAmountToPay(SubscriptionType subscriptionType, int noOfScreensRequired) {
        int amountPerScreen;
        switch (subscriptionType) {
            case BASIC:
                amountPerScreen = 200;
                break;
            case PRO:
                amountPerScreen = 250;
                break;
            case ELITE:
                amountPerScreen = 350;
                break;
            default:
                amountPerScreen = 0;
        }
        return calculateBaseSubscriptionCost(subscriptionType) + (amountPerScreen * noOfScreensRequired);
    }


    private int calculateBaseSubscriptionCost(SubscriptionType subscriptionType) {
        switch (subscriptionType) {
            case BASIC:
                return 500; // $500 for BASIC plan
            case PRO:
                return 800; // $800 for PRO plan
            case ELITE:
                return 1000; // $1000 for ELITE plan
            default:
                return 0;
        }
    }

    public Integer upgradeSubscription(Integer userId)throws Exception{

        //If you are already at an ElITE subscription : then throw Exception ("Already the best Subscription")

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription currentSubscription = user.getSubscription();

        if (currentSubscription.getSubscriptionType() == SubscriptionType.ELITE) {
            throw new Exception("Already the best Sgtubscription");
        }

        //In all other cases just try to upgrade the subscription and tell the difference of price that user has to pay
        //update the subscription in the repository

        int differenceInPrice = calculateUpgradePrice(currentSubscription.getSubscriptionType());

        SubscriptionType newSubscriptionType = getNextSubscriptionType(currentSubscription.getSubscriptionType());
        currentSubscription.setSubscriptionType(newSubscriptionType);
        subscriptionRepository.save(currentSubscription);

        return differenceInPrice;
    }


    private int calculateUpgradePrice(SubscriptionType currentSubscriptionType) {
        switch (currentSubscriptionType) {
            case BASIC:
                return 300; // Upgrade from BASIC to PRO costs $300
            case PRO:
                return 200; // Upgrade from PRO to ELITE costs $200
            default:
                return 0;
        }
    }

    private SubscriptionType getNextSubscriptionType(SubscriptionType currentSubscriptionType) {
        switch (currentSubscriptionType) {
            case BASIC:
                return SubscriptionType.PRO;
            case PRO:
                return SubscriptionType.ELITE;
            default:
                return currentSubscriptionType;
        }
    }

    public Integer calculateTotalRevenueOfHotstar(){

        //We need to find out total Revenue of hotstar : from all the subscriptions combined
        //Hint is to use findAll function from the SubscriptionDb

        List<Subscription> subscriptions = subscriptionRepository.findAll();
        int totalRevenue = 0;
        for (Subscription subscription : subscriptions) {
            totalRevenue += subscription.getTotalAmountPaid();
        }
        return totalRevenue;
    }

}
