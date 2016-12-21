package org.hisp.dhis.dataset;

/*
 * Copyright (c) 2004-2015, University of Oslo
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

import static org.hisp.dhis.common.IdentifiableObjectUtils.getUids;
import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Tri
 * @version $Id$
 */
@Transactional
public class DefaultSectionService
    implements SectionService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SectionStore sectionStore;

    public void setSectionStore( SectionStore sectionStore )
    {
        this.sectionStore = sectionStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

   @Autowired
    private OrganisationUnitService organisationUnitService;
    
    @Autowired
    private CurrentUserService currentUserService;
	
    // -------------------------------------------------------------------------
    // SectionService implementation
    // -------------------------------------------------------------------------

    @Override
    public int addSection( Section section )
    {
        int id = sectionStore.save( section );

        return id;
    }

    @Override
    public void deleteSection( Section section )
    {
        sectionStore.delete( section );
    }

    @Override
    public List<Section> getAllSections()
    {
        return i18n( i18nService, sectionStore.getAll() );
    }

    @Override
    public Section getSection( int id )
    {
        return i18n( i18nService, sectionStore.get( id ) );
    }

    @Override
    public Section getSection( String uid )
    {
        return i18n( i18nService, sectionStore.getByUid( uid ) );
    }

    @Override
    public Section getSectionByName( String name, Integer dataSetId )
    {
        return i18n( i18nService, sectionStore.getSectionByName( name, dataSetService.getDataSet( dataSetId ) ) );
    }

    @Override
    public void updateSection( Section section )
    {
        sectionStore.update( section );
    }
	
    @Override
    public void mergeWithCurrentUserOrganisationUnits(Section section, Collection<OrganisationUnit> mergeOrganisationUnits) 
    {
    	Set<OrganisationUnit> organisationUnits = new HashSet<OrganisationUnit>( section.getSources() );
    
    	 Set<OrganisationUnit> userOrganisationUnits = new HashSet<OrganisationUnit>();
    	
    	 List<String> parentIds = getUids( currentUserService.getCurrentUser().getOrganisationUnits() );
    	 
    	 userOrganisationUnits = new HashSet<OrganisationUnit>( organisationUnitService.getOrganisationUnitsWithChildren( parentIds, null) );
    	 
         //List<OrganisationUnit> organisationUnitsWithChildren = getOrganisationUnitsWithChildren( parentIds, maxLevels );
    	
    	/*
    	for ( OrganisationUnit organisationUnit : currentUserService.getCurrentUser().getOrganisationUnits() )
    	{
    	    //userOrganisationUnits.addAll( organisationUnitService.getOrganisationUnitsWithChildren( organisationUnit.getUid() ) );
    	    //userOrganisationUnits.addAll( organisationUnitService.getOrganisationUnitWithChildren( organisationUnit.getId() ) );
    	}
        */
         
    	organisationUnits.removeAll( userOrganisationUnits );
    	organisationUnits.addAll( mergeOrganisationUnits );
     
    	section.setSources( organisationUnits );
     
    	updateSection(section);
    }	
}
