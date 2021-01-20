use E2
go
select l.log_id_high, l.log_id_low, l.log_type, d.log_subt, d.log_data
from pslog l, pslogdat d
where l.log_id_high = d.log_id_high and l.log_id_low = d.log_id_low
order by l.log_id_high, l.log_id_low, d.log_seq, d.log_subseq
go
