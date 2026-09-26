# Lab 04 - Adapter Design Pattern

## Project Description

This project demonstrates the Object Adapter Design Pattern in Java.

The project integrates legacy smart home devices with the modern
`SmartDevice` interface without modifying the original legacy classes.

## Project Structure

- `SmartDevice.java` - Target interface
- `LegacyBulb.java` - Legacy bulb
- `LegacyThermostat.java` - Legacy thermostat
- `BulbAdapter.java` - Adapter for LegacyBulb
- `ThermostatAdapter.java` - Adapter for LegacyThermostat
- `ModernHub.java` - Client that controls SmartDevice objects
- `Main.java` - Integration and testing

## AI Usage

AI was used as a learning and development assistant during this lab.

AI was used to:
- understand the lab requirements;
- understand the Object Adapter Design Pattern;
- assist with implementing and reviewing the adapter code;
- explain the interaction between the classes;
- check the implementation against the lab requirements;
- help understand and resolve errors.

The final implementation was reviewed and tested in IntelliJ IDEA.