
/* App Module */
var trackerCapture = angular.module('trackerCapture',
        ['ui.bootstrap', 
         'ngRoute', 
         'ngCookies',
         'ngSanitize',
         'ngMessages',
         'trackerCaptureServices',
         'trackerCaptureFilters',
         'trackerCaptureDirectives',
         'd2Directives',
         'd2Filters',
         'd2Services',
         'd2Controllers',
         'angularLocalStorage',
         'ui.select',
         'ui.select2',
         'infinite-scroll',
         'd2HeaderBar',
         'sticky',
         'nvd3ChartDirectives',
         'pascalprecht.translate'])
              
.value('DHIS2URL', '../api')

.config(function($httpProvider, $routeProvider, $translateProvider) {    
            
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
    
    $routeProvider.when('/', {
        /*
        templateUrl:'views/home.html',
        controller: 'SelectionController'
        */
      // change for Saint Lucia start

      templateUrl:'ehs/ehs-home/ehs-home.html',
      controller: 'EHSHomeTypesController'
      // for food safety program start
    }).when('/food-safety-program', {
      templateUrl: 'ehs/food-safety-program/food-safety-program.html',
      controller: 'FoodSafetyProgramController'


    }).when('/establishments-registration', {
      templateUrl: 'ehs/food-safety-program/establishments/establishments-home.html',
      controller: 'EstablishmentsSelectionController'


    }).when('/operators-registration', {
      templateUrl: 'ehs/food-safety-program/operators/operators-home.html',
      controller: 'OperatorsSelectionController'
    // for food safety program end


    // for water safety program start
    }).when('/water-safety-program', {
        templateUrl: 'ehs/water-safety-program/water-safety-program.html',
        controller: 'WaterSafetyProgramController'

    }).when('/water-establishments-registration', {
        templateUrl: 'ehs/water-safety-program/water-establishments/water-establishments-home.html',
        controller: 'WaterEstablishmentsSelectionController'

    // for water safety program start
    // change for Saint Lucia end

    }).when('/dashboard',{
        templateUrl:'components/dashboard/dashboard.html',
        controller: 'DashboardController'
    }).when('/report-types',{
        templateUrl:'views/report-types.html',
        controller: 'ReportTypesController'
    }).when('/program-summary',{
        templateUrl:'components/report/program-summary.html',
        controller: 'ProgramSummaryController'
    }).when('/program-statistics',{
        templateUrl:'components/report/program-statistics.html',
        controller: 'ProgramStatisticsController'
    }).when('/overdue-events',{
        templateUrl:'components/report/overdue-events.html',
        controller: 'OverdueEventsController'
    }).when('/upcoming-events',{
        templateUrl:'components/report/upcoming-events.html',
        controller: 'UpcomingEventsController'
    }).otherwise({
        redirectTo : '../dhis-web-commons/security/login.action'
    });  
    
    $translateProvider.preferredLanguage('en');
    $translateProvider.useSanitizeValueStrategy('escaped');
    $translateProvider.useLoader('i18nLoader');
    
})

.run(function($templateCache, $http, $rootScope){    
    $http.get('components/dataentry/inner-form.html').then(function(page){        
        $templateCache.put('components/dataentry/inner-form.html', page.data);
    });
    $http.get('components/dataentry/section-inner-form.html').then(function(page){        
        $templateCache.put('components/dataentry/section-inner-form.html', page.data);
    });
    
    $rootScope.maxGridColumnSize = 1;
    $rootScope.maxOptionSize = 30;
});
