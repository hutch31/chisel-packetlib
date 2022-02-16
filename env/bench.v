module bench;
  reg clock;
  reg reset;  

  /*AUTOWIRE*/

  /* FlatPacketBuffer AUTO_TEMPLATE
   (
   .io_buf_writeReqOut_\(.*\) (writeRing1_\1[]),
   .io_buf_writeReqIn_\(.*\)  (writeRing0_\1[]),
   );
   */
  FlatPacketBuffer buffer
    (/*AUTOINST*/);

/*  PacketWriter AUTO_TEMPLATE
 (
 .io_id (2'd@),
 .io_interface_writeReqOut_\(.*\) (writeRing@_\1[]),
 .io_interface_writeReqIn_\(.*\) (writeRing@"(+ @ 1)"_\1[]),
 );
 */
  PacketWriter writer0
    (/*AUTOINST*/);

/*  PacketReader AUTO_TEMPLATE
 (
 .io_id (2'd@),
 .io_interface_bufferReadResp_bits_data_\([0-9]+\)      (io_buf_readRespOut_bits_data_\1[]),
 .io_buf_readRespOut_bits_req_line (io_interface_bufferReadResp_bits_req_line[1:0]),
 );
 */
  PacketReader reader0
    (/*AUTOINST*/);
  
endmodule // bench
// Local Variables:
// verilog-library-directories:("." "generated")
// End:
