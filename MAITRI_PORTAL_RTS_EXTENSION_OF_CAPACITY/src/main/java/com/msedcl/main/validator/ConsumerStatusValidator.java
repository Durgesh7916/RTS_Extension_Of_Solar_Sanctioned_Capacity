package com.msedcl.main.validator;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsumerStatusValidator {

    /**
     * Validates the consumer number and consumer status.
     *
     * @param conn           Existing database connection
     * @param consumerNumber Consumer number to validate
     * @return null if valid; otherwise, an error message
     */
    public static String validateConsumerStatus(
            Connection conn, String consumerNumber) throws SQLException {

        if (consumerNumber == null || consumerNumber.trim().isEmpty()) {
            return "ERROR: Consumer Number cannot be blank.";
        }

        consumerNumber = consumerNumber.trim();

        String sql = "SELECT Consumer_Status "
                   + "FROM MBC_CONSUMER_MASTER "
                   + "WHERE Consumer_Number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, consumerNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return "ERROR: Consumer Data Not Found.";
                }

                String consumerStatus = rs.getString("Consumer_Status");

                if (consumerStatus != null
                        && !consumerStatus.equalsIgnoreCase("0")
                        && !consumerStatus.equalsIgnoreCase("C")
                        && !consumerStatus.equalsIgnoreCase("1")
                        && !consumerStatus.equalsIgnoreCase("T")) {

                    return "ERROR: Consumer " + consumerNumber + " is PD.";
                }
            }
        }

        return null;
    }
    
    

public static String validateSolarConsumer(
        Connection conn, String consumerNumber) throws SQLException {

    if (consumerNumber == null || consumerNumber.trim().isEmpty()) {
        return "ERROR: Consumer Number cannot be blank.";
    }

    String sql = "SELECT COUNT(*) "
               + "FROM MBC_CONSUMER_MASTER "
               + "WHERE CONSUMER_NUMBER = ? "
               + "AND UPPER(TRIM(SOLAR_ROOFTOP_FLAG)) IN ('Y','B','C','G')";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, consumerNumber.trim());

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {

                return "ERROR: Please provide a valid Solar Rooftop Consumer. "
                     + "Given consumer is not eligible for Extension of Solar "
                     + "Sanction Capacity because it is not a Solar Consumer.";
            }
        }
    }

    return null;
}



public static String validateExistingSolarExtensionApplication(
        Connection conn, String consumerNumber) throws SQLException {

    if (consumerNumber == null || consumerNumber.trim().isEmpty()) {
        return "ERROR: Consumer Number cannot be blank.";
    }

    String sql = "SELECT APPLICATION_ID_N, APPLN_SERVICE_TYPE_ID_N "
               + "FROM NC_APPLICATION "
               + "WHERE ASSIGNED_CONSUMER_NO_C = ? "
               + "AND CURRENT_WF_STATUS_ID_N IN "
               + "(1,2,4,5,6,8,9,10,11,13,14,16,17) "
               + "AND APPLN_SERVICE_TYPE_ID_N = 40";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, consumerNumber.trim());

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                String applicationId = rs.getString("APPLICATION_ID_N");
                String serviceType = rs.getString("APPLN_SERVICE_TYPE_ID_N");

                return "ERROR: An application for Extension of Solar Rooftop "
                     + "Capacity is already available for Consumer Number "
                     + consumerNumber.trim()
                     + ". Existing Application ID: " + applicationId
                     + ", Service Type: " + serviceType
                     + "(Extension of Solar Rooftop Capacity)"
                     + ". Please Complete The  existing application Workflow.";
            }
        }
    }

    return null;
}


}
