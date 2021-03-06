package com.hevelian.olastic.core.api.uri.queryoption.expression.member.impl;

import com.hevelian.olastic.core.api.uri.queryoption.expression.member.ExpressionMember;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.core.uri.parser.UriTokenizer;
import org.apache.olingo.server.core.uri.parser.UriTokenizer.TokenKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps olingo literal data. All methods in this class are used when literal is
 * at the left position of binary expression: '25' gt age In this case operation
 * is reversed: age lt '25' and execution is delegated to the left part member
 *
 * @author Taras Kohut
 */
public class LiteralMember extends BaseMember {

    private String value;
    private EdmType edmType;

    public LiteralMember(String value, EdmType edmType) {
        if (edmType instanceof EdmString && (!value.startsWith("'") || !value.endsWith("'"))) {
            throw new IllegalArgumentException(
                    "String values should be enclosed in single quotation marks");
        }
        this.edmType = edmType;
        this.value = value;
    }

    /**
     * Checks the edm type of the string value, and creates concrete type from
     * this value
     *
     * @return converted value
     */
    public Object getValue() {
        UriTokenizer tokenizer = new UriTokenizer(value);
        if (tokenizer.next(UriTokenizer.TokenKind.StringValue) && edmType instanceof EdmString) {
            return value.substring(1, value.length() - 1).replaceAll("''", "'");
        } else if (tokenizer.next(TokenKind.jsonArrayOrObject) && edmType == null) {
            String arrayAsString = value.substring(1, value.length() - 1);
            List<String> values = new ArrayList<>();
            for (String string : arrayAsString.split(",")) {
                values.add(string.replace("\"", ""));
            }
            return values;
        } else if (tokenizer.next(TokenKind.NULL)){
            return null;
        }
        else {
            return value;
        }
    }

    @Override
    public ExpressionMember eq(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.eq(this);
    }

    @Override
    public ExpressionMember ne(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.ne(this);
    }

    @Override
    public ExpressionMember ge(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.le(this);
    }

    @Override
    public ExpressionMember gt(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.lt(this);
    }

    @Override
    public ExpressionMember le(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.ge(this);
    }

    @Override
    public ExpressionMember lt(ExpressionMember expressionMember) throws ODataApplicationException {
        return expressionMember.gt(this);
    }
}
