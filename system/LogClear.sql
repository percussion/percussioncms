use E2
go
truncate table pslogdat
go
delete from pslog
go
dump tran E2 with NO_LOG
go
