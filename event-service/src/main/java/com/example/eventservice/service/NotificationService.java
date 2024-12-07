package com.example.eventservice.service;

import com.example.eventservice.event.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    public final JavaMailSender javaMailSender;

    @KafkaListener(topics = "booking-confirmed")
    public void listen(BookingConfirmedEvent bookingConfirmedEvent) {

        log.info("Received message from booking-confirmed topic: {}", bookingConfirmedEvent);

        // Prepare email
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("kodexbaba41@gmail.com"); // Externalize this
            messageHelper.setTo(bookingConfirmedEvent.getEmail());
            messageHelper.setSubject(String.format("Room with ID: (%s) booked successfully", bookingConfirmedEvent.getRoomId()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedStartTime = bookingConfirmedEvent.getStartTime().format(formatter);

            messageHelper.setText(String.format("""
                    Good Day %s,

                    You booked %s successfully for %s.

                    Have a great event!
                    Kodex Baba
                    """,
                    bookingConfirmedEvent.getUserName(),
                    bookingConfirmedEvent.getRoomName(),
                    formattedStartTime));
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Booking notification successfully sent to {}", bookingConfirmedEvent.getEmail());
        } catch (MailException e) {
            log.error("Failed to send email to {} for room {}", bookingConfirmedEvent.getEmail(), bookingConfirmedEvent.getRoomId(), e);
            throw new RuntimeException("Exception occurred when attempting to send email", e);
        }
    }
}