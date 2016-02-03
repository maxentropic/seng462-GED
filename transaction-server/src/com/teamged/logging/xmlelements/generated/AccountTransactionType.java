//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.02 at 07:58:07 PM PST 
//


package com.teamged.logging.xmlelements.generated;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AccountTransactionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccountTransactionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="timestamp" type="{}unixTimeLimits"/>
 *         &lt;element name="server" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionNum" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="action" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="funds" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountTransactionType", propOrder = {

})
public class AccountTransactionType {

    @XmlSchemaType(name = "integer")
    protected long timestamp;
    @XmlElement(required = true)
    protected String server;
    @XmlElement(required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger transactionNum;
    @XmlElement(required = true)
    protected String action;
    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected BigDecimal funds;

    /**
     * Gets the value of the timestamp property.
     * 
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     */
    public void setTimestamp(long value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the server property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the value of the server property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServer(String value) {
        this.server = value;
    }

    /**
     * Gets the value of the transactionNum property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTransactionNum() {
        return transactionNum;
    }

    /**
     * Sets the value of the transactionNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTransactionNum(BigInteger value) {
        this.transactionNum = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the funds property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getFunds() {
        return funds;
    }

    /**
     * Sets the value of the funds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setFunds(BigDecimal value) {
        this.funds = value;
    }

}
