
$(document).ready(function ()
{
    $.ajax(
    {   
        url: "getattackhistory",
        method: "post",
        dataType: "json",
        success: function (data)
        {
            $("table tbody").empty();
            for (var i = 0; i < data.length; i++)
            {
                var tr = $("<tr>");

                var d = new Date(data[i].time);
                
                var td = $("<td>");
                td.html(d.getDate() + "/"
                    + (d.getMonth()+1) + "/"
                    + d.getFullYear() + " "
                    + (d.getHours() < 10 ? "0" : "") + d.getHours() + ":"
                    + (d.getMinutes() < 10 ? "0" : "") + d.getMinutes() + ":"
                    + (d.getSeconds() < 10 ? "0" : "") + d.getSeconds());
                tr.append(td);
                
                var td = $("<td>");
                td.html(data[i].isAttacker ? "Attacker" : "Defender");
                tr.append(td);
                
                var td = $("<td>");
                td.html(data[i].isAttacker ? data[i].defender : data[i].attacker);
                tr.append(td);
                
                var td = $("<td>");
                td.html(parseFloat(data[i].result * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaStrengthA * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaLandA * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaEnergyA * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaStrengthD * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaLandD * 100).toFixed(1) + "%");
                tr.append(td);

                var td = $("<td>");
                td.html(parseFloat(data[i].deltaEnergyD * 100).toFixed(1) + "%");
                tr.append(td);

                $("table tbody").append(tr);
            }
        }
    }); 
});
