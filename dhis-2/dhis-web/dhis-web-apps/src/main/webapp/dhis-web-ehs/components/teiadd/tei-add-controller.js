/* global trackerCapture, angular */

var trackerCapture = angular.module('trackerCapture');
trackerCapture.controller('TEIAddController',
    function($scope,
             $rootScope,
             $translate,
             $modalInstance,
             $location,
             AjaxCalls,
             DateUtils,
             $modal,
             CurrentSelection,
             OperatorFactory,
             AttributesFactory,
             EntityQueryFactory,
             OrgUnitFactory,
             ProgramFactory,
             MetaDataFactory,
             TEIService,
             TEIGridService,
             DialogService,
             Paginator,
             relationshipTypes,
             selectedProgram,
             relatedProgramRelationship,
             selections,
             selectedAttribute,
             existingAssociateUid,
             addingRelationship,
             selectedTei){


        $scope.operatorsProgram = 'ieLe1vT4Vad';

        $scope.selectedTei = {};
        $scope.attributesById = CurrentSelection.getAttributesById();
        if(!$scope.attributesById){
            $scope.attributesById = [];
            AttributesFactory.getAll().then(function(atts){
                angular.forEach(atts, function(att){
                    $scope.attributesById[att.id] = att;
                });

                CurrentSelection.setAttributesById($scope.attributesById);
            });
        }

        $scope.optionSets = CurrentSelection.getOptionSets();
        if(!$scope.optionSets){
            $scope.optionSets = [];
            MetaDataFactory.getAll('optionSets').then(function(optionSets){
                angular.forEach(optionSets, function(optionSet){
                    $scope.optionSets[optionSet.id] = optionSet;
                });

                CurrentSelection.setOptionSets($scope.optionSets);
            });
        }

        $scope.showTeiList = true;

        $scope.today = DateUtils.getToday();
        $scope.relationshipTypes = relationshipTypes;
        $scope.addingRelationship = addingRelationship;
        $scope.selectedAttribute = selectedAttribute;
        $scope.selectedProgram = selectedProgram;
        $scope.relatedProgramRelationship = relatedProgramRelationship;
        $scope.mainTei = selectedTei;
        $scope.attributesById = CurrentSelection.getAttributesById();
        $scope.addingTeiAssociate = false;

        $scope.searchOuTree = false;
        $scope.orgUnitLabel = $translate.instant('org_unit');

        $scope.selectedRelationship = {};
        $scope.relationship = {};

        var invalidTeis = $scope.addingRelationship ? [$scope.mainTei.trackedEntityInstance] : [];
        if($scope.mainTei.relationships && $scope.addingRelationship){
            angular.forEach($scope.mainTei.relationships, function(rel){
                invalidTeis.push(rel.trackedEntityInstanceB);
            });
        }

        OrgUnitFactory.getOrgUnit(($location.search()).ou).then(function(orgUnit) {
            $scope.selectedOrgUnit = orgUnit;
            $scope.selectedEnrollment = {
                enrollmentDate: $scope.today,
                incidentDate: $scope.today,
                orgUnitName: $scope.selectedOrgUnit.displayName
            };

            //Selections
            $scope.selectedTeiForDisplay = angular.copy($scope.mainTei);
            $scope.ouModes = [{name: 'SELECTED'}, {name: 'CHILDREN'}, {name: 'DESCENDANTS'}, {name: 'ACCESSIBLE'}];
            //$scope.selectedOuMode = $scope.ouModes[0];
            $scope.selectedOuMode = $scope.ouModes[2];//  for saint lucia

            //Paging
            $scope.pager = {pageSize: 50, page: 1, toolBarDisplay: 5};

            //Searching
            $scope.showAdvancedSearchDiv = false;
            $scope.searchText = {value: null};
            $scope.emptySearchText = false;
            $scope.searchFilterExists = false;
            $scope.defaultOperators = OperatorFactory.defaultOperators;
            $scope.boolOperators = OperatorFactory.boolOperators;
            $scope.selectedTrackedEntity = null;

            $scope.trackedEntityList = null;
            $scope.enrollment = {programStartDate: '', programEndDate: '', operator: $scope.defaultOperators[0]};

            $scope.searchMode = {listAll: 'LIST_ALL', freeText: 'FREE_TEXT', attributeBased: 'ATTRIBUTE_BASED'};
            $scope.selectedSearchMode = $scope.searchMode.listAll;

            if ($scope.addingRelationship) {
                $scope.teiAddLabel = $translate.instant('add_relationship');
                $scope.programs = selections.prs;
                CurrentSelection.setRelationshipOwner($scope.mainTei);
            }
            else {
                $scope.teiAddLabel = $scope.selectedAttribute && $scope.selectedAttribute.displayName ? $scope.selectedAttribute.displayName : $translate.instant('tracker_associate');
                $scope.addingTeiAssociate = true;
                ProgramFactory.getProgramsByOu($scope.selectedOrgUnit, $scope.selectedProgram).then(function (response) {
                    $scope.programs = response.programs;
                    if ($scope.selectedAttribute && $scope.selectedAttribute.trackedEntity && $scope.selectedAttribute.trackedEntity.id) {
                        $scope.programs = [];
                        angular.forEach(response.programs, function (pr) {
                            if (pr.trackedEntity && pr.trackedEntity.id === $scope.selectedAttribute.trackedEntity.id) {
                                $scope.programs.push(pr);
                            }
                        });

                        // for saint lucia
                        for(var i=0; i<response.programs.length; i++){
                            if(response.programs[i].id === $scope.operatorsProgram ){
                                $scope.setAttributesForSearch(response.programs[i]);
                            }
                        }
                    }

                    $scope.selectedProgram = response.selectedProgram;
                    //$scope.setAttributesForSearch($scope.programs[0]);// for saint lucia


                });

                if (existingAssociateUid) {
                    TEIService.get(existingAssociateUid, $scope.optionSets, $scope.attributesById).then(function (data) {
                        $scope.selectedTeiForDisplay = data;
                    });
                }
                else {
                    $scope.selectedTeiForDisplay = null;
                }

                CurrentSelection.setRelationshipOwner({});

                if ($scope.selectedAttribute && $scope.selectedAttribute.trackedEntity && $scope.selectedAttribute.trackedEntity.id) {
                    $scope.selectedTrackedEntity = $scope.selectedAttribute.trackedEntity;
                }
            }

            if (angular.isObject($scope.programs) && $scope.programs.length === 1) {
                $scope.selectedProgramForRelative = $scope.programs[0];
            }

            if ($scope.selectedProgram) {
                if ($scope.selectedProgram.relatedProgram && $scope.relatedProgramRelationship) {
                    angular.forEach($scope.programs, function (pr) {
                        if (pr.id === $scope.selectedProgram.relatedProgram.id) {
                            $scope.selectedProgramForRelative = pr;
                        }
                    });
                }

                if ($scope.selectedProgram.relationshipType) {
                    angular.forEach($scope.relationshipTypes, function (rel) {
                        if (rel.id === $scope.selectedProgram.relationshipType.id) {
                            $scope.relationship.selected = rel;
                        }
                    });
                }
            }

            if( $scope.selectedProgram.id === 'TeBSCKYRo3q')
            {
                $scope.showTeiList = false;
            }

            //watch for selection of relationship
            $scope.$watch('relationship.selected', function () {
                if (angular.isObject($scope.relationship.selected)) {
                    $scope.selectedRelationship = {
                        aIsToB: $scope.relationship.selected.aIsToB,
                        bIsToA: $scope.relationship.selected.bIsToA
                    };
                }
            });


            /*$scope.getTrackerAssociate = function (selectedAttribute, existingAssociateUid) {
                var modalInstance = $modal.open({
                    templateUrl: 'components/addestablishment/addAssociation1.html',
                    controller: 'AddAssociationController1',
                    windowClass: 'modal-full-window',
                    resolve: {

                    }
                });

               // modalInstance.selectedEvent = $scope.selectedEvent;

             //   var value = utilityService.extractMetaAttributeValue($scope.programStagesMap[$scope.selectedEvent.programStage].attributeValues,"areMultipleAssociationAllowed")
               // modalInstance.areMultipleAssociationAllowed = value;

                modalInstance.result.then(function (res) {
                    if (res && res.id) {
                        $scope.selectedTei = {};
                        $scope.selectedTei[selectedAttribute.id] = res.id;

                        $scope.ownerName = res.MaCVsKG7pjb;
                        $scope.ownerName1 = res.id;
                       // TEIService.putselectedtei(res.id);
                        AjaxCalls.setselectedtei(res.id);
                        AjaxCalls.setselectedattributeid(selectedAttribute.id);
                        /* if(res && res.tyXd890iVJG) {
                         $scope.ownerName = res.tyXd890iVJG;
                         }
                         else {
                         $scope.ownerName =" ";
                         }*/
                    /*}
                });
            };*/
            function resetFields() {

                $scope.teiForRelationship = null;
                $scope.teiFetched = false;
                $scope.emptySearchText = false;
                $scope.emptySearchAttribute = false;
                $scope.showAdvancedSearchDiv = false;
                $scope.showRegistrationDiv = false;
                $scope.showTrackedEntityDiv = false;
                $scope.trackedEntityList = null;
                $scope.teiCount = null;

                $scope.queryUrl = null;
                $scope.programUrl = null;
                $scope.attributeUrl = {url: null, hasValue: false};
                $scope.sortColumn = {};
            }

            //listen for selections
            $scope.$on('relationship', function (event, args) {
                if (args.result === 'SUCCESS') {
                    var relationshipInfo = CurrentSelection.getRelationshipInfo();
                    $scope.teiForRelationship = relationshipInfo.tei;
                    $scope.addRelationship();
                }

                if (args.result === 'CANCEL') {
                    $scope.showRegistration();
				   
                }
            });

            //sortGrid
            $scope.sortGrid = function (gridHeader) {
                if ($scope.sortColumn && $scope.sortColumn.id === gridHeader.id) {
                    $scope.reverse = !$scope.reverse;
                    return;
                }
                $scope.sortColumn = gridHeader;
                if ($scope.sortColumn.valueType === 'date') {
                    $scope.reverse = true;
                }
                else {
                    $scope.reverse = false;
                }
            };

            $scope.d2Sort = function (tei) {
                if ($scope.sortColumn && $scope.sortColumn.valueType === 'date') {
                    var d = tei[$scope.sortColumn.id];
                    return DateUtils.getDate(d);
                }
                return tei[$scope.sortColumn.id];
            };

            $scope.search = function (mode) {

                resetFields();

                $scope.selectedSearchMode = mode;

                if ($scope.selectedProgramForRelative) {
                    $scope.programUrl = 'program=' + $scope.selectedProgramForRelative.id;
                }

                //check search mode
                if ($scope.selectedSearchMode === $scope.searchMode.freeText) {

                    if (!$scope.searchText.value) {
                        $scope.search('LIST_ALL');
                        $scope.emptySearchText = true;
                        $scope.teiFetched = false;
                        $scope.teiCount = null;
                        return;
                    }

                    $scope.queryUrl = 'query=LIKE:' + $scope.searchText.value;
                }

                if ($scope.selectedSearchMode === $scope.searchMode.attributeBased) {
                    $scope.searchText.value = null;
                    $scope.attributeUrl = EntityQueryFactory.getAttributesQuery($scope.attributes, $scope.enrollment);

                    if (!$scope.attributeUrl.hasValue && !$scope.selectedProgramForRelative) {
                        $scope.emptySearchAttribute = true;
                        $scope.teiFetched = false;
                        $scope.teiCount = null;
                        return;
                    }
                }

                if ($scope.addingTeiAssociate) {
                    if (!$scope.selectedTrackedEntity || !$scope.selectedTrackedEntity.id) {
                        var dialogOptions = {
                            headerText: 'searching_error',
                            bodyText: $translate.instant('no_entity_for_tracker_associate_attribute')
                        };
                        DialogService.showDialog({}, dialogOptions);
                        $scope.teiFetched = true;
                        return;
                    }

                    //$scope.programUrl = 'trackedEntity=' + $scope.selectedTrackedEntity.id;
                }

                $scope.fetchTei();
            };

            $scope.fetchTei = function () {

                //get events for the specified parameters
                TEIService.search($scope.selectedOrgUnit.id,
                    $scope.selectedOuMode.name,
                    $scope.queryUrl,
                    $scope.programUrl,
                    $scope.attributeUrl.url,
                    $scope.pager,
                    true).then(function (data) {
                        //$scope.trackedEntityList = data;
                        if (data.rows) {
                            $scope.teiCount = data.rows.length;
                        }

                        if (data.metaData.pager) {
                            $scope.pager = data.metaData.pager;
                            $scope.pager.toolBarDisplay = 5;

                            Paginator.setPage($scope.pager.page);
                            Paginator.setPageCount($scope.pager.pageCount);
                            Paginator.setPageSize($scope.pager.pageSize);
                            Paginator.setItemCount($scope.pager.total);
                        }

                        //process tei grid

                        TEIGridService.format(data, false, $scope.optionSets, invalidTeis).then(function (response) {
                            $scope.trackedEntityList = response;
                            $scope.showTrackedEntityDiv = true;
                            $scope.teiFetched = true;

                            if (!$scope.sortColumn.id) {
                                $scope.sortGrid({
                                    id: 'created',
                                    name: $translate.instant('registration_date'),
                                    valueType: 'date',
                                    displayInListNoProgram: false,
                                    showFilter: false,
                                    show: false
                                });
                            }
                        });
                    });
            };

            //set attributes as per selected program
            $scope.setAttributesForSearch = function (program) {

                $scope.selectedProgramForRelative = program;
                AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function (atts) {
                    $scope.attributes = atts;
                    $scope.attributes = AttributesFactory.generateAttributeFilters(atts);
                    $scope.gridColumns = TEIGridService.generateGridColumns($scope.attributes, null, false).columns;
                });

                $scope.search($scope.selectedSearchMode);
            };

            $scope.setAttributesForSearch($scope.selectedProgramForRelative);

            $scope.jumpToPage = function () {
                if ($scope.pager && $scope.pager.page && $scope.pager.pageCount && $scope.pager.page > $scope.pager.pageCount) {
                    $scope.pager.page = $scope.pager.pageCount;
                }

                $scope.search($scope.selectedSearchMode);
            };

            $scope.resetPageSize = function () {
                $scope.pager.page = 1;
                $scope.search($scope.selectedSearchMode);
            };

            $scope.getPage = function (page) {
                $scope.pager.page = page;
                $scope.search($scope.selectedSearchMode);
            };

            //generate grid columns from teilist attributes
            $scope.generateGridColumns = function (attributes) {

                var columns = attributes ? angular.copy(attributes) : [];

                //also add extra columns which are not part of attributes (orgunit for example)
                columns.push({id: 'orgUnitName', name: 'Organisation unit', type: 'TEXT', displayInListNoProgram: false});
                columns.push({id: 'created', name: 'Registration date', type: 'TEXT', displayInListNoProgram: false});

                //generate grid column for the selected program/attributes
                angular.forEach(columns, function (column) {
                    if (column.id === 'orgUnitName' && $scope.selectedOuMode.name !== 'SELECTED') {
                        column.show = true;
                    }

                    if (column.displayInListNoProgram) {
                        column.show = true;
                    }

                    if (column.type === 'date') {
                        $scope.filterText[column.id] = {start: '', end: ''};
                    }
                });
                return columns;
            };

            $scope.showHideSearch = function (simpleSearch) {
                $scope.showAdvancedSearchDiv = simpleSearch ? false : !$scope.showAdvancedSearchDiv;
                $scope.showTrackedEntityDiv = !$scope.showAdvancedSearchDiv;
            };

            $scope.showRegistration = function () {
                $scope.showRegistrationDiv = !$scope.showRegistrationDiv;
                $scope.showTrackedEntityDiv = !$scope.showRegistrationDiv;
            };

            $scope.close = function () {
                $modalInstance.close($scope.mainTei.relationships ? $scope.mainTei.relationships : []);
                $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
            };

            $scope.setRelationshipSides = function (side) {
                if (side === 'A') {
                    $scope.selectedRelationship.bIsToA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.relationship.selected.bIsToA : $scope.relationship.selected.aIsToB;
                }
                if (side === 'B') {
                    $scope.selectedRelationship.aIsToB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.relationship.selected.aIsToB : $scope.relationship.selected.bIsToA;
                }
            };

            $scope.assignRelationship = function (relativeTei) {
                $scope.teiForRelationship = relativeTei;
                $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
            };

            $scope.back = function () {
                $scope.teiForRelationship = null;
                $rootScope.showAddRelationshipDiv = !$rootScope.showAddRelationshipDiv;
            };

            $scope.addRelationship = function () {
                if ($scope.addingRelationship) {
                    if ($scope.mainTei && $scope.teiForRelationship && $scope.relationship.selected) {
                        var tei = angular.copy($scope.mainTei);
                        var relationship = {};
                        relationship.relationship = $scope.relationship.selected.id;
                        relationship.displayName = $scope.relationship.selected.displayName;
                        relationship.relative = {};

                        relationship.trackedEntityInstanceA = $scope.selectedRelationship.aIsToB === $scope.relationship.selected.aIsToB ? $scope.mainTei.trackedEntityInstance : $scope.teiForRelationship.id;
                        relationship.trackedEntityInstanceB = $scope.selectedRelationship.bIsToA === $scope.relationship.selected.bIsToA ? $scope.teiForRelationship.id : $scope.mainTei.trackedEntityInstance;

                        tei.relationships = [];
                        angular.forEach($scope.mainTei.relationships, function (rel) {
                            tei.relationships.push({
                                relationship: rel.relationship,
                                displayName: rel.displayName,
                                trackedEntityInstanceA: rel.trackedEntityInstanceA,
                                trackedEntityInstanceB: rel.trackedEntityInstanceB
                            });
                        });
                        tei.relationships.push(relationship);

                        TEIService.update(tei, $scope.optionSets, $scope.attributesById).then(function (response) {
                            if (response.response && response.response.status !== 'SUCCESS') {//update has failed
                                var dialogOptions = {
                                    headerText: 'relationship_error',
                                    bodyText: response.message
                                };
                                DialogService.showDialog({}, dialogOptions);
                                return;
                            }

                            relationship.relative.processed = true;
                            relationship.relative.attributes = $scope.teiForRelationship;

                            if ($scope.mainTei.relationships) {
                                $scope.mainTei.relationships.push(relationship);
                            }
                            else {
                                $scope.mainTei.relationships = [relationship];
                            }

                            $modalInstance.close($scope.mainTei.relationships);
                        });
                    }
                    else {
                        var dialogOptions = {
                            headerText: 'relationship_error',
                            bodyText: $translate.instant('selected_tei_is_invalid')
                        };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }
                }
                else {
                    if ($scope.teiForRelationship && $scope.teiForRelationship.id) {
                        $modalInstance.close($scope.teiForRelationship);
                    }
                    else {
                        var dialogOptions = {
                            headerText: 'tracker_associate_error',
                            bodyText: $translate.instant('selected_tei_is_invalid')
                        };
                        DialogService.showDialog({}, dialogOptions);
                        return;
                    }

                }
            };

            //Get orgunits for the logged in user
            OrgUnitFactory.getSearchTreeRoot().then(function (response) {
                $scope.orgUnits = response.organisationUnits;
                angular.forEach($scope.orgUnits, function (ou) {
                    ou.show = true;
                    angular.forEach(ou.children, function (o) {
                        o.hasChildren = o.children && o.children.length > 0 ? true : false;
                    });
                });
            });

            //expand/collapse of search orgunit tree
            $scope.expandCollapse = function (orgUnit) {
                if (orgUnit.hasChildren) {
                    //Get children for the selected orgUnit
                    OrgUnitFactory.get(orgUnit.id).then(function (ou) {
                        orgUnit.show = !orgUnit.show;
                        orgUnit.hasChildren = false;
                        orgUnit.children = ou.organisationUnits[0].children;
                        angular.forEach(orgUnit.children, function (ou) {
                            ou.hasChildren = ou.children && ou.children.length > 0 ? true : false;
                        });
                    });
                }
                else {
                    orgUnit.show = !orgUnit.show;
                }
            };

            //load programs for the selected orgunit (from tree)
            $scope.setSelectedSearchingOrgUnit = function (orgUnit) {
                $scope.selectedSearchingOrgUnit = orgUnit;
            };
        });
    })

    .controller('TEIRegistrationController',
    function($rootScope,
             $modal,
             $scope,
             AjaxCalls,
             $timeout,
             AttributesFactory,
             MetaDataFactory,
             TrackerRulesFactory,
             CustomFormService,
             TEService,
             EnrollmentService,
             DialogService,
             CurrentSelection,
             DateUtils,
             EventUtils,
             DHIS2EventFactory,
             RegistrationService,
             SessionStorageService,
             TrackerRulesExecutionService,
             TEIGridService,
             $translate,
             OrganisationUnitService,
             EHSService ) {
				 
		 

        $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
        $scope.enrollment = {enrollmentDate: '', incidentDate: ''};
        $scope.attributesById = CurrentSelection.getAttributesById();
        $scope.today = DateUtils.getToday();
        $scope.trackedEntityForm = null;
        $scope.customForm = null;
        $scope.selectedTei = {};

        $scope.teiOriginal = {};
        $scope.tei = {};
        $scope.hiddenFields = {};
        //$scope.editingDisabled = false;

        var selections = CurrentSelection.get();
       // $scope.programs = selections.prs;
		$scope.programs = undefined;
		var selectedatributeid=AjaxCalls.getselectedattributeid();

        $scope.selectedTei[selectedatributeid] = AjaxCalls.getselectedtei();


        // add for saint Lucia
        $scope.rootOrgUnit = null;
        $scope.districtOrgUnits = {};
        $scope.communityOrgUnits = {};
        $scope.selectedDistrict = null;
        $scope.selectedCommunity = null;

        $scope.selectedCountryName = null;
        $scope.selectedDistrictName = null;
        $scope.selectedCommunityName = null;
        $scope.countryList = [];
        $scope.tempCountryList = [];
        $scope.tempOrgUnit=[];
        $scope.optionSetUid = 'RyHTAVjHjGt';
		
		$scope.selectedCountry = "SAINT LUCIA";

        //$scope.getDistrict( $scope.selectedCountry );

        $scope.init = function () {
            $scope.getDistrict( $scope.selectedCountry );
        };

        OrganisationUnitService.getRootOrganisationUnit().then(function(rootOrganisationUnit){
            $scope.rootOrgUnit = rootOrganisationUnit.organisationUnits[0];
            $scope.selectedCountryName = rootOrganisationUnit.organisationUnits[0].name;
        });

        OrganisationUnitService.getLevel2OrganisationUnit().then(function(level2OrgUnit){
            $scope.districtOrgUnits = level2OrgUnit.organisationUnits;

        });

        OrganisationUnitService.getRootOrganisationUnit().then(function(rootOrganisationUnit){
            $scope.rootOrgUnit = rootOrganisationUnit.organisationUnits[0];

            $scope.tempOrgUnit['code'] = rootOrganisationUnit.organisationUnits[0].name;
            $scope.tempOrgUnit['name'] = rootOrganisationUnit.organisationUnits[0].name;
            $scope.tempCountryList.push($scope.tempOrgUnit);

            EHSService.getOptionsByOptionSet( $scope.optionSetUid ).then(function( optionMembers ){
                $scope.optionSetOptions = optionMembers.options;
                $scope.countryList = $scope.tempCountryList.concat($scope.optionSetOptions);
            });

        });

        $scope.getDistrict = function( selectedCountry ) { 
           
            //alert( selectedCountry );
            $scope.selectedCountry1=selectedCountry;
            $scope.selectedCountryName = selectedCountry;

            $scope.districtOrgUnits = {};
            $scope.communityOrgUnits = {};
            //$scope.selectedCountry1=selectedcountry;
            $scope.tempSelectedCountry = selectedCountry;
            $scope.tempSelectedCountry1 = selectedCountry;
			
            /*if( selectedCountry == $scope.rootOrgUnit.name ){
                //  $scope.selectedCountry1=selectedcountry.displayName;
                OrganisationUnitService.getLevel2OrganisationUnit().then(function(level2OrgUnit){
                    $scope.districtOrgUnits = level2OrgUnit.organisationUnits;

                });
            }
            else{
                $scope.selectedDistrict1="";
                $scope.selectedCommunity1="";
                $scope.selectedCommunityUid = $scope.rootOrgUnit.id;
            }*/
			
			if( $scope.rootOrgUnit != null )
            {
                if( selectedCountry == $scope.rootOrgUnit.name ){
                    //  $scope.selectedCountry1=selectedcountry.displayName;
                    OrganisationUnitService.getLevel2OrganisationUnit().then(function(level2OrgUnit){
                        $scope.districtOrgUnits = level2OrgUnit.organisationUnits;

                    });
                }
                else{
                    $scope.selectedDistrict1="";
                    $scope.selectedCommunity1="";
                    $scope.selectedCommunityUid = 'Jgc02tIKupW';
                }
            }
            else
            {
                if( selectedCountry === 'SAINT LUCIA' ){
                    OrganisationUnitService.getLevel2OrganisationUnit().then(function(level2OrgUnit){
                        $scope.districtOrgUnits = level2OrgUnit.organisationUnits;

                    });
                }
                else{
                    $scope.selectedDistrict1="";
                    $scope.selectedCommunity1="";
                    $scope.selectedCommunityUid = $scope.rootOrgUnit.id;
                }
            }
        };

        $scope.getAnotherDistrict = function( anotherDistrictName ){
            $scope.selectedDistrict1=anotherDistrictName;
            //alert( anotherDistrictName );
            $scope.selectedCommunityUid = $scope.rootOrgUnit.id;
            $scope.selectedDistrictName = anotherDistrictName;
        };

        $scope.getCommunity = function( districtUid ){

            if( districtUid === null)
            {
                //alert(districtUid);
                $scope.communityOrgUnits = {};

            }

            else
            {
                OrganisationUnitService.getChildrenOrganisationUnits( districtUid ).then(function(communityOrganisationUnits){
                    $scope.communityOrgUnits = communityOrganisationUnits.children;

                    OrganisationUnitService.getOrganisationUnitObject( districtUid ).then(function(orgUnitObject){
                        //$scope.selectedDistrict = orgUnitObject;
                        $scope.selectedDistrictName = orgUnitObject.displayName;
                        $scope.selectedDistrict1=orgUnitObject.id;

                    });

                });
            }

        };

        $scope.getAnotherCommunity = function( anotherCommunityName ){
            //alert( anotherCommunityName );
            $scope.selectedCommunity1=anotherCommunityName;
            $scope.selectedCommunityUid = $scope.rootOrgUnit.id;
            $scope.selectedCommunityName = anotherCommunityName;
        };

        $scope.getEnrollingOrgunit = function( enrollingOrgUnitUid ){
            //alert(enrollingOrgUnitUid);
            $scope.selectedCommunity = enrollingOrgUnitUid;

            OrganisationUnitService.getOrganisationUnitObject( enrollingOrgUnitUid ).then(function( organisationUnitObject ){
                //$scope.selectedCommunity = organisationUnitObjectObject;
                $scope.selectedOrgUnit = organisationUnitObject;
                $scope.selectedCommunityName = organisationUnitObject.displayName;

            });
        };
        // end

        $scope.attributesById = CurrentSelection.getAttributesById();
        if(!$scope.attributesById){
            $scope.attributesById = [];
            AttributesFactory.getAll().then(function(atts){
                angular.forEach(atts, function(att){
                    $scope.attributesById[att.id] = att;
                });

                CurrentSelection.setAttributesById($scope.attributesById);
            });
        }

        $scope.optionSets = CurrentSelection.getOptionSets();
        if(!$scope.optionSets){
            $scope.optionSets = [];
            MetaDataFactory.getAll('optionSets').then(function(optionSets){
                angular.forEach(optionSets, function(optionSet){
                    $scope.optionSets[optionSet.id] = optionSet;
                });

                CurrentSelection.setOptionSets($scope.optionSets);
            });
        }
		
        var assignInheritance = function(){
            $scope.selectedTei = {};
            if($scope.addingRelationship){
                var t = angular.copy( CurrentSelection.getRelationshipOwner() );
                angular.forEach(t.attributes, function(att){
                    t[att.attribute] = att.value;
                });

                angular.forEach($scope.attributes, function(att){
                    if(att.inherit && t[att.id]){
                        $scope.selectedTei[att.id] = t[att.id];
                    }
                });
                t = {};
            }
        };

        var getRules = function(){
            $scope.allProgramRules = {constants: [], programIndicators: {}, programValidations: [], programVariables: [], programRules: []};
            if( angular.isObject($scope.selectedProgramForRelative) && $scope.selectedProgramForRelative.id ){
                TrackerRulesFactory.getRules($scope.selectedProgramForRelative.id).then(function(rules){
                    $scope.allProgramRules = rules;
                });
            }
        };


        $scope.isDisabled = function(attribute) {

            if( attribute.code === 'current_license_status' || attribute.code === 'current_license_expiry_date' )
            {
                return true;
            }
        };

        if(angular.isObject($scope.programs) && $scope.programs.length === 1){
            $scope.selectedProgramForRelative = $scope.programs[0];
            AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
                $scope.attributes = TEIGridService.generateGridColumns(atts, null,false).columns;
                assignInheritance();
                getRules();
            });
        }

        //watch for selection of program
        $scope.$watch('selectedProgramForRelative', function() {
            $scope.trackedEntityForm = null;
            $scope.customForm = null;
            $scope.customFormExists = false;
            AttributesFactory.getByProgram($scope.selectedProgramForRelative).then(function(atts){
                $scope.attributes = TEIGridService.generateGridColumns(atts, null,false).columns;
                if($scope.selectedProgramForRelative && $scope.selectedProgramForRelative.id && $scope.selectedProgramForRelative.dataEntryForm && $scope.selectedProgramForRelative.dataEntryForm.htmlCode){
                    $scope.customFormExists = true;
                    $scope.trackedEntityForm = $scope.selectedProgramForRelative.dataEntryForm;
                    $scope.trackedEntityForm.attributes = $scope.attributes;
                    $scope.trackedEntityForm.selectIncidentDatesInFuture = $scope.selectedProgramForRelative.selectIncidentDatesInFuture;
                    $scope.trackedEntityForm.selectEnrollmentDatesInFuture = $scope.selectedProgramForRelative.selectEnrollmentDatesInFuture;
                    $scope.trackedEntityForm.displayIncidentDate = $scope.selectedProgramForRelative.displayIncidentDate;
                    $scope.customForm = CustomFormService.getForTrackedEntity($scope.trackedEntityForm, 'RELATIONSHIP');
                }
                assignInheritance();
                getRules();
            });

        });
		

        $scope.getTrackerAssociate2 = function (selectedAttribute, existingAssociateUid) {
            var modalInstance = $modal.open({
                templateUrl: 'components/addestablishment/addAssociation1.html',
                controller: 'AddAssociationController1',
                windowClass: 'modal-full-window',
                resolve: {
                    relationshipTypes: function () {
                        return $scope.relationshipTypes;
                    },
                    addingRelationship: function () {
                        return false;
                    },
                    selections: function () {
                        return CurrentSelection.get();
                    },
                    selectedTei: function () {
                        return $scope.selectedTei;
                    },
                    selectedAttribute: function () {
                        return selectedAttribute;
                    },
                    existingAssociateUid: function () {
                        return existingAssociateUid;
                    },
                    selectedProgram: function () {
                        return $scope.selectedProgram;
                    },
                    relatedProgramRelationship: function () {
                        return $scope.relatedProgramRelationship;
                    }
                }
            });
            modalInstance.result.then(function (res) {
                if (res && res.id) {
                    $scope.selectedTei[selectedAttribute.id] = res.id;

                    $scope.ownerName = res.MaCVsKG7pjb;
                    /* if(res && res.tyXd890iVJG) {
                     $scope.ownerName = res.tyXd890iVJG;
                     }
                     else {
                     $scope.ownerName =" ";
                     }*/
                }
            });
			
        };
















        $scope.trackedEntities = {available: []};
        TEService.getAll().then(function(entities){
            $scope.trackedEntities.available = entities;
            $scope.trackedEntities.selected = $scope.trackedEntities.available[0];
        });

        $scope.registerEntity = function(){

            //check for form validity
            $scope.outerForm.submitted = true;
            if( $scope.outerForm.$invalid ){
                return false;
            }

            //form is valid, continue the registration
            //get selected entity
            var selectedTrackedEntity = "";
            if( $scope.trackedEntities.available.length > 0 )
            {
                selectedTrackedEntity = $scope.trackedEntities.selected.id;
            }

            if($scope.selectedProgramForRelative){
                selectedTrackedEntity = $scope.selectedProgramForRelative.trackedEntity.id;
            }

            //get tei attributes and their values
            //but there could be a case where attributes are non-mandatory and
            //registration form comes empty, in this case enforce at least one value
            $scope.selectedTei.trackedEntity = $scope.tei.trackedEntity = selectedTrackedEntity;
            $scope.selectedTei.orgUnit = $scope.tei.orgUnit = $scope.selectedOrgUnit.id;
            $scope.selectedTei.attributes = $scope.tei.attributes = [];

            //$scope.selectedCountryName;
            //$scope.selectedDistrictName;
            //$scope.selectedCommunityName;
            //var result = RegistrationService.processForm($scope.tei, $scope.selectedTei, $scope.teiOriginal, $scope.attributesById);
            //add for saint lucia
			

            if( $scope.selectedCountryName == null )
            {
                var dialogOptions = {
                    headerText: 'registration_error',
                    bodyText: $translate.instant('please_select_country_name')
                };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
            else if( $scope.selectedDistrictName == null )
            {
                var dialogOptions = {
                    headerText: 'registration_error',
                    bodyText: $translate.instant('please_select_district_name')
                };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
            else if( $scope.selectedCommunityName == null )
            {
                var dialogOptions = {
                    headerText: 'registration_error',
                    bodyText: $translate.instant('please_select_community_name')
                };
                DialogService.showDialog({}, dialogOptions);
                return;
            }
            else
            {
                var result = RegistrationService.processForm($scope.tei, $scope.selectedTei, $scope.teiOriginal, $scope.attributesById, $scope.selectedCountryName, $scope.selectedDistrictName, $scope.selectedCommunityName);
                $scope.formEmpty = result.formEmpty;
                $scope.tei = result.tei;

                if($scope.formEmpty){//registration form is empty
                    return false;
                }
            }

            RegistrationService.registerOrUpdate($scope.tei, $scope.optionSets, $scope.attributesById).then(function(registrationResponse){
                var reg = registrationResponse.response ? registrationResponse.response : {};
                if(reg.reference && reg.status === 'SUCCESS'){
                    $scope.tei.trackedEntityInstance = $scope.tei.id = reg.reference;

                    //registration is successful and check for enrollment
                    if($scope.selectedProgramForRelative){
                        //enroll TEI
                        var enrollment = {};
                        enrollment.trackedEntityInstance = $scope.tei.trackedEntityInstance;
                        enrollment.program = $scope.selectedProgramForRelative.id;
                        enrollment.status = 'ACTIVE';
                        enrollment.orgUnit = $scope.selectedOrgUnit.id;
                        enrollment.enrollmentDate = $scope.selectedEnrollment.enrollmentDate;
                        enrollment.incidentDate = $scope.selectedEnrollment.incidentDate === '' ? $scope.selectedEnrollment.enrollmentDate : $scope.selectedEnrollment.incidentDate;
                        EnrollmentService.enroll(enrollment).then(function(enrollmentResponse){
                            var en = enrollmentResponse.response && enrollmentResponse.response.importSummaries && enrollmentResponse.response.importSummaries[0] ? enrollmentResponse.response.importSummaries[0] : {};
                            if(en.reference && en.status === 'SUCCESS'){
                                enrollment.enrollment = en.reference;
                                $scope.selectedEnrollment = enrollment;
                                var dhis2Events = EventUtils.autoGenerateEvents($scope.tei.trackedEntityInstance, $scope.selectedProgramForRelative, $scope.selectedOrgUnit, enrollment, null);
                                if(dhis2Events.events.length > 0){
                                    DHIS2EventFactory.create(dhis2Events).then(function(){
                                    });
                                }
                            }
                            else{
                                //enrollment has failed
                                var dialogOptions = {
                                    headerText: 'enrollment_error',
                                    bodyText: enrollmentResponse.message
                                };
                                DialogService.showDialog({}, dialogOptions);
                                return;
                            }
                        });
                    }
                }
                else{
                    //registration has failed
                    var dialogOptions = {
                        headerText: 'registration_error',
                        bodyText: registrationResponse.message
                    };
                    DialogService.showDialog({}, dialogOptions);
                    return;
                }

                $timeout(function(){
                    $scope.selectedEnrollment.enrollmentDate = '';
                    $scope.selectedEnrollment.incidentDate =  '';
                    $scope.outerForm.submitted = false;
                    $scope.broadCastSelections();
                }, 100);

            });
        };

        $scope.broadCastSelections = function(){
            if($scope.tei){
                angular.forEach($scope.tei.attributes, function(att){
                    $scope.tei[att.attribute] = att.value;
                });

                $scope.tei.orgUnitName = $scope.selectedOrgUnit.displayName;
                $scope.tei.created = DateUtils.formatFromApiToUser(new Date());

                CurrentSelection.setRelationshipInfo({tei: $scope.tei});

                $timeout(function() {
                    $rootScope.$broadcast('relationship', {result: 'SUCCESS'});
                }, 100);
            }
        };

        $scope.executeRules = function () {
            var flag = {debug: true, verbose: false};

            //repopulate attributes with updated values
            $scope.selectedTei.attributes = [];
            angular.forEach($scope.attributes, function(metaAttribute){
                var newAttributeInArray = {attribute:metaAttribute.id,
                    code:metaAttribute.code,
                    displayName:metaAttribute.displayName,
                    type:metaAttribute.valueType
                };
                if($scope.selectedTei[newAttributeInArray.attribute]){
                    newAttributeInArray.value = $scope.selectedTei[newAttributeInArray.attribute];
                }

                $scope.selectedTei.attributes.push(newAttributeInArray);
            });

            if($scope.selectedProgram && $scope.selectedProgram.id){
                TrackerRulesExecutionService.executeRules($scope.allProgramRules, 'registration', null, null, $scope.selectedTei, $scope.selectedEnrollment, flag);
            }
        };

        //check if field is hidden
        $scope.isHidden = function (id) {
            //In case the field contains a value, we cant hide it.
            //If we hid a field with a value, it would falsely seem the user was aware that the value was entered in the UI.
            return $scope.selectedTei[id] ? false : $scope.hiddenFields[id];
        };

        $scope.teiValueUpdated = function(tei, field){
            $scope.executeRules();
        };

        //listen for rule effect changes
        $scope.$on('ruleeffectsupdated', function(){
            $scope.warningMessages = [];
            var effectResult = TrackerRulesExecutionService.processRuleEffectAttribute('registration', $scope.selectedTei, $scope.tei, $scope.attributesById, $scope.hiddenFields, $scope.warningMessages);
            $scope.selectedTei = effectResult.selectedTei;
            $scope.hiddenFields = effectResult.hiddenFields;
            $scope.warningMessages = effectResult.warningMessages;
        });

        $scope.interacted = function(field) {
            var status = false;
            if(field){
                status = $scope.outerForm.submitted || field.$dirty;
            }
            return status;
        };
    });
