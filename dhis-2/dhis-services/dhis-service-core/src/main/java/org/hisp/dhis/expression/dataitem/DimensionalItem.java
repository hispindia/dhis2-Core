/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.expression.dataitem;

import static org.hisp.dhis.parser.expression.ParserUtils.DOUBLE_VALUE_IF_NULL;
import static org.hisp.dhis.parser.expression.antlr.ExpressionParser.ExprContext;

import org.hisp.dhis.antlr.ParserExceptionWithoutContext;
import org.hisp.dhis.common.DimensionalItemId;
import org.hisp.dhis.common.DimensionalItemObject;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.common.ValueTypedDimensionalItemObject;
import org.hisp.dhis.parser.expression.CommonExpressionVisitor;
import org.hisp.dhis.parser.expression.ExpressionItem;
import org.hisp.dhis.system.util.ValidationUtils;

/**
 * Parsed dimensional item as handled by the expression service.
 *
 * @author Jim Grace
 */
public abstract class DimensionalItem
    implements ExpressionItem
{
    @Override
    public final Object getDescription( ExprContext ctx, CommonExpressionVisitor visitor )
    {
        DimensionalItemId itemId = getDimensionalItemId( ctx, visitor );

        DimensionalItemObject item = visitor.getDimensionService().getDataDimensionalItemObject( itemId );

        if ( item == null )
        {
            throw new ParserExceptionWithoutContext(
                "Can't find " + itemId.getDimensionItemType().name() + " for '" + itemId + "'" );
        }

        visitor.getItemDescriptions().put( ctx.getText(), item.getDisplayName() );

        return ValidationUtils.getNullReplacementValue( getItemValueType( item ) );
    }

    @Override
    public final Object getItemId( ExprContext ctx, CommonExpressionVisitor visitor )
    {
        visitor.getItemIds().add( getDimensionalItemId( ctx, visitor ) );

        return DOUBLE_VALUE_IF_NULL;
    }

    @Override
    public final Object getOrgUnitGroup( ExprContext ctx, CommonExpressionVisitor visitor )
    {
        return DOUBLE_VALUE_IF_NULL;
    }

    @Override
    public final Object evaluate( ExprContext ctx, CommonExpressionVisitor visitor )
    {
        DimensionalItemId itemId = getDimensionalItemId( ctx, visitor );

        DimensionalItemObject item = visitor.getDimItemMap().get( itemId );

        Object value = (item != null)
            ? visitor.getItemValueMap().get( item )
            : null;

        return visitor.handleNulls( value, getItemValueType( item ) );
    }

    /**
     * Constructs the DimensionalItemId object for this item.
     *
     * @param ctx the parser item context
     * @param visitor the tree visitor
     * @return the DimensionalItemId object for this item
     */
    public abstract DimensionalItemId getDimensionalItemId( ExprContext ctx,
        CommonExpressionVisitor visitor );

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Returns the value type of this item.
     */
    private ValueType getItemValueType( DimensionalItemObject item )
    {
        return (item instanceof ValueTypedDimensionalItemObject)
            ? ((ValueTypedDimensionalItemObject) item).getValueType()
            : ValueType.NUMBER;
    }
}
