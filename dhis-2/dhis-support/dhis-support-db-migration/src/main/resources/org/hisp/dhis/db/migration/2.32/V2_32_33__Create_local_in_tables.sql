--Create table config_in
create table if not exists config_in (
    configid integer NOT NULL,
    ckey character varying(160) NOT NULL,
    cvalue character varying(160) NOT NULL
);

--Create table reportin
create table if not exists reportin (
    reportid integer NOT NULL,
    name character varying(160) NOT NULL,
    model character varying(100) NOT NULL,
    periodtypeid integer NOT NULL,
    excelname character varying(100) NOT NULL,
    xmlname character varying(100) NOT NULL,
    reporttype character varying(100) NOT NULL,
    orgunitgroupid bigint,
    datasetids character varying(100)
);

--Create table reportsource
create table if not exists reportsource (
    reportid integer NOT NULL,
    sourceid bigint NOT NULL
);


--Droping existing constraints in config_in
alter table config_in 
drop constraint if exists config_in_pkey,
drop constraint if exists uk_config_in_ckey;

--Adding constraints for config_in
alter table config_in
add constraint config_in_pkey primary key (configid),
add constraint uk_config_in_ckey unique (ckey);


--Droping existing constraints in reportin
alter table reportin 
drop constraint if exists reportin_pkey,
drop constraint if exists uk_reportin_name;
drop constraint if exists fk_reportin_orgunitgroupid;
drop constraint if exists fk_reportin_periodtypeid;

--Adding constraints for reportin
alter table reportin
add constraint reportin_pkey primary key (reportid),
add constraint uk_reportin_name unique (name),
add constraint fk_reportin_orgunitgroupid foreign key (orgunitgroupid) references orgunitgroup(orgunitgroupid),
add constraint fk_reportin_periodtypeid foreign key (periodtypeid) references periodtype(periodtypeid);

--Droping existing constraints in reportsource
alter table reportsource 
drop constraint if exists reportsource_pkey,
drop constraint if exists fk_reportsource_reportid;
drop constraint if exists fk_reportsource_sourceid;


--Adding constraints for reportsource
alter table reportsource
add constraint reportsource_pkey primary key (reportid, sourceid),
add constraint fk_reportsource_reportid foreign key (reportid) references reportin(reportid),
add constraint fk_reportsource_sourceid foreign key (sourceid) references organisationunit(organisationunitid);
