use E2
go
drop table pslogdat
go
drop table pslog
go
create table pslog(
   log_id_high int,
   log_id_low int,
   log_type tinyint,
   log_appl int,
   constraint pslog_pkey primary key (log_id_high, log_id_low))
go
create table pslogdat(
   log_id_high int,
   log_id_low int,
   log_seq tinyint,
   log_subt int,
   log_subseq tinyint,
   log_data varchar(255),
   constraint pslogdat_pkey primary key (log_id_high, log_id_low, log_seq, log_subseq))
go
